package protocols.voteCondorcet.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * classe d'un agent qui soumet un appel au vote a d'autres agents  par le protocole ContractNet
 *
 * 【Condorcet 投票站】用 Contract-Net 协议组织投票，核心是**Condorcet 算法**——
 * 两两对决胜场数 + 平票处理。
 *
 * 【算法三步走】
 *   1. **构建对决矩阵**：`votes[i][j]` = 有多少票把 i 排在 j 之前（正数=赢，负数=输）
 *   2. **算每个选项的胜负统计**：
 *      - `duelsGagnes[i]` = 有多少个 j 满足 votes[i][j] > 0（i 打败了几个选项）
 *      - `sommePoints[i]` = 该行元素之和（i 的净胜场）
 *   3. **选胜者**：duelsGagnes 最大者；平票则按 sommePoints；再平票则**在两两对决子集上重跑**；
 *      再平票则**随机抽取**
 *
 * 关键对比：
 *   - Borda 用"排名累加"，Condorcet 用"两两对决"——后者更符合"多数偏好"直觉
 *   - 参考 [PollingStationAgentOld](PollingStationAgentOld.java) 是旧版本，缺少"子集重跑"这一步
 *
 * @author eadam
 */
public class PollingStationAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     *
     * 【setup】只建窗口，投票由按钮触发
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a vote
     *
     * 【创建投票协议行为】核心方法：构造 CFP → 找参与者 → 挂 ContractNetInitiator → 做 Condorcet 计算
     */
    private void createVote(String id, String objet) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("debut d'un vote pour les options " + objet);
        // 【把候选项字符串切成数组】
        String[] options = objet.split(",");
        int dim = options.length;
        // 【对决矩阵】votes[i][j] 表示 i 对 j 的胜场差（正=i赢，负=i输）
        final int[][] votes = new int[dim][dim];
        println("-".repeat(30));

        // 【构造 CFP】
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        // 【服务发现】找 vote/participant 服务下的所有投票者
        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("destinataires trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());
        println("-".repeat(40));

        // 【声明 Contract-Net 协议】必须设置，否则响应方不会按协议处理
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        // 【挂载协议发起行为】
        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            // 【每收到一个 PROPOSE】只打印
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("l'agent %s propose %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            // 【收到拒绝】
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUS ! j'ai recu un refus  de " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            // 【所有响应收齐后触发】这里做全部 Condorcet 计算
            @Override
            protected void handleAllResponses(List<ACLMessage> leursVotes, List<ACLMessage> mesRetours) {
                ArrayList<ACLMessage> listeVotes = new ArrayList<>(leursVotes);
                //on ne garde que les propositions
                // 【过滤掉 REFUSE 等非 PROPOSE 消息】
                listeVotes.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);
                // 【每位投票者的排序结果】
                String[][] strMatVotes = new String[listeVotes.size()][dim];

                var listeOptions = Arrays.asList(options);

                for (int i=0; i<listeVotes.size(); i++)
                {
                    ACLMessage vote = listeVotes.get(i);
                    //par defaut, on accepte tout vote
                    // 【为每个投票者准备好 ACCEPT_PROPOSAL 回复】
                    var retour = vote.createReply();
                    retour.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    mesRetours.add(retour);
                    var content = vote.getContent();
                    //analyse du contenu sous la forme resto1>resto2>,...
                    // 【解析排序】"option2>option4>option1" → ["option2","option4","option1"]
                    strMatVotes[i] = content.split(">");
                }

                // 【核心：遍历每个投票者的排序，累加到对决矩阵】
                for(String[]sesVotes:strMatVotes)
                {
                    String strChoixI, strChoixJ;

                    // 【双重循环遍历所有 (i, j) 组合，i 排在 j 前面】
                    for (int i=0; i<sesVotes.length-1; i++) {
                        strChoixI = sesVotes[i];
                        int oi = listeOptions.indexOf(strChoixI);
                        for (int j=i+1; j<sesVotes.length; j++) {
                            strChoixJ = sesVotes[j];
                            int oj = listeOptions.indexOf(strChoixJ);
                            // 【oi 排在 oj 前面：oi 得 +1，oj 得 -1】
                            votes[oi][oj] += 1;
                            votes[oj][oi] -= 1;
                        }
                    }
                }

                println("-".repeat(40));
                //affichage du total des votes
                // 【打印对决矩阵】
                println("resultat des votes : ");
                for(int[]ligne:votes) println(Arrays.toString(ligne));

                // on regarde s'il existe une option gagnante (preferee des autres)
                // 【统计每个选项的胜负】
                //   duelsGagnes[i] = 有多少个选项被 i 打败（正数计数）
                //   sommePoints[i] = 该行元素之和（净胜场）
                int[]duelsGagnes = new int[dim];
                final int[]sommePoints = new int[dim];
                for(int i=0; i<dim; i++) {
                    duelsGagnes[i] = (int)Arrays.stream(votes[i]).filter(j->j>0).count();
                    sommePoints[i] = Arrays.stream(votes[i]).sum();
                }


                println("nb de duels gagnes / options : "+ Arrays.toString(duelsGagnes));
                println("points / options : "+ Arrays.toString(sommePoints));

                // 【第一步：按"胜场数"选】
                int maxMax = Arrays.stream(duelsGagnes).max().orElse(0);
                var selectedOptions = new ArrayList<Integer>();
                for(int i=0; i<dim; i++) if (duelsGagnes[i]==maxMax)selectedOptions.add(i);

                // 【默认取第一个】
                String[]  electedOption = {options[selectedOptions.get(0)]};
                StringBuilder sbMax = new StringBuilder("Options ayant gagnes le plus de duels :");
                selectedOptions.forEach(i->sbMax.append(options[i]).append(", "));
                println(sbMax.toString());

                // 【第二步：如果胜场数平票，按"净胜场（分数）"再选】
                if(selectedOptions.size()>1){
                    println("=".repeat(30));
                    println("ex-aequo !");
                    // 【只在平票候选集里比分数】
                    int[] sommePointsSelected = new int[dim];
                    Arrays.setAll(sommePointsSelected, i-> selectedOptions.contains(i)?sommePoints[i]:0);
                    println("max de points parmi " + Arrays.toString(sommePointsSelected));
                    maxMax = Arrays.stream(sommePointsSelected).max().orElse(0);
                    selectedOptions.clear();
                    for(int i=0; i<dim; i++) if (sommePointsSelected[i]==maxMax)selectedOptions.add(i);
                    StringBuilder sbTotal = new StringBuilder("Options ayant obtenu le plus de points : ");
                    selectedOptions.forEach(i->sbTotal.append(options[i]).append(", "));
                    println(sbTotal.toString());
                    electedOption[0] = options[selectedOptions.get(0)];

                    // 【第三步：如果分数也平票，在平票子集上重跑一次 Condorcet】
                    //   清空矩阵，只重算"平票选项之间"的对决
                    if(selectedOptions.size()>1) {
                        println("=".repeat(30));
                        println("encore un ex-aequo ! => condorcet limite a ces options...");
                        for(int[] ligne:votes)Arrays.fill(ligne, 0);

                        // 【遍历所有投票，只保留"两个选项都在平票集内"的对决】
                        for (ACLMessage vote : listeVotes) {
                            var content = vote.getContent();
                            //analyse du contenu sous la forme resto1>resto2>,...
                            String[] sesVotes = content.split(">");
                            String strChoixI, strChoixJ;

                            for (int i=0; i<sesVotes.length-1; i++) {
                                strChoixI = sesVotes[i];
                                int oi = listeOptions.indexOf(strChoixI);
                                if(selectedOptions.contains(oi)) {
                                    for (int j = i + 1; j < sesVotes.length; j++) {
                                        strChoixJ = sesVotes[j];
                                        int oj = listeOptions.indexOf(strChoixJ);
                                        if(selectedOptions.contains(oj)) {
                                            votes[oi][oj] += 1;
                                            votes[oj][oi] -= 1;
                                        }
                                    }
                                }
                            }
                        }

                            // 【重新统计胜负】
                            // on regarde s'il existe une option gagnante (preferee des autres)
                            Arrays.fill(duelsGagnes, 0);
                            Arrays.fill(sommePoints, 0);
                            for(int i=0; i<dim; i++) {
                                duelsGagnes[i] = (int)Arrays.stream(votes[i]).filter(j->j>0).count();
                                sommePoints[i] = Arrays.stream(votes[i]).sum();
                            }


                            println("nb de duels gagnes / options : "+ Arrays.toString(duelsGagnes));
                            println("points / options : "+ Arrays.toString(sommePoints));

                            // 【子集内的胜场数最大值】
                            maxMax = Arrays.stream(duelsGagnes).max().orElse(0);
                            var newSelectedOptions = new ArrayList<Integer>();
                            for(int i=0; i<dim; i++) if (duelsGagnes[i]==maxMax && selectedOptions.contains(i))newSelectedOptions.add(i);

                            electedOption[0] = options[newSelectedOptions.get(0)];
                            StringBuilder sbMax2 = new StringBuilder("Options ayant gagnes le plus de duels :");
                            newSelectedOptions.forEach(i->sbMax2.append(options[i]).append(", "));
                            println(sbMax2.toString());

                            // 【第四步：如果子集内还平票，用分数再筛一次】
                            if(newSelectedOptions.size()>1) {
                                println("=".repeat(30));
                                println("ex-aequo !");
                                Arrays.setAll(sommePointsSelected, i -> newSelectedOptions.contains(i) ? sommePoints[i] : 0);
                                println("max de points parmi " + Arrays.toString(sommePointsSelected));
                                maxMax = Arrays.stream(sommePointsSelected).max().orElse(0);
                                newSelectedOptions.clear();
                                for (int i = 0; i < dim; i++)
                                    if (sommePointsSelected[i] == maxMax && selectedOptions.contains(i)) newSelectedOptions.add(i);
                                StringBuilder sbTotal2 = new StringBuilder("Options ayant obtenu le plus de points : ");
                                newSelectedOptions.forEach(i -> sbTotal2.append(options[i]).append(", "));
                                println(sbTotal2.toString());
                                electedOption[0] = options[newSelectedOptions.get(0)];

                                // 【第五步：最终平票——随机抽取一个】
                                if(newSelectedOptions.size()>1) {
                                    println("encore un ex-aequo ! => tirage aleatoire entre ces options...");
                                    electedOption[0] = options[selectedOptions.stream().findAny().orElse(0)];
                                    println("Choix final : " + electedOption[0]);
                                }

                            }
                    }

                }

                // 【把所有回复的 content 填成当选选项】所有投票者都会收到相同结果
                mesRetours.forEach(msg -> msg.setContent(electedOption[0]));
                println("#-".repeat(30));
            }


        };

        addBehaviour(init);

    }

    // 【按钮事件】触发投票
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    // 【生成候选列表】把 Resto 枚举的所有值拼成 "Pizzeria,Vegetables,Sushi,..."
    public void launchRequest() {
        StringBuilder sb = new StringBuilder();
        for (Resto r : Resto.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

    // 【takeDown】下线时关窗口
    @Override
    public void takeDown() {
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
        window.dispose();
    }

}
