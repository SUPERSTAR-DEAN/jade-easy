package protocols.negociation;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * 【买方行为】讨价还价中买方这一侧的决策逻辑。
 *
 * 判断规则：
 *   - `对方出价 <= 我方上次出价` → 同意（对方开始让步）
 *   - `对方出价 > 我方阈值(110)` → 拒绝（价格太高）
 *   - 否则 → 我方还价，价格在上次基础上再涨 5%
 *   - 达到 10 轮还没谈成 → 发 FAILURE 表示超时
 *
 * 与 [CompVendeur](CompVendeur.java) 对称：卖方在"降到对方之上"，买方在"涨到对方之下"。
 */
public class CompAcheteur extends Behaviour {

    // 【轮数计数】每收到一个消息 +1
    int step = 0;
    // 【是否已同意】
    boolean accord;
    // 【是否已拒绝】
    boolean rejet;
    // 【对方的出价】
    double offreAutre;
    // 【我方上次出价】初值 0，表示"还没出价"
    double offrePrecedente;
    // 【我方本次出价】
    double offre;
    // 【让步步长】0.05 = 每次涨 5%
    double epsilon = 0.05;
    // 【引用智能体】
    Negociateur monAgent;
    // 【消息过滤器】只接收会话 ID = "MARCHE" 的消息
    MessageTemplate modele;

    // 【构造】把 agent 和模板传进来，同时设定"我方阈值"
    CompAcheteur(Negociateur monAgent, MessageTemplate modele){
        super(monAgent);
        this.monAgent = monAgent;
        this.modele = modele;
        monAgent.seuil = 110;   // 【买方阈值】出价超过 110 就拒绝
    }

    @Override
    public void action() {
        // 【接收】没消息就 block 挂起
        var msg = myAgent.receive(modele);
        if (msg != null) {
            // 【先看是否是终态消息】
            switch (msg.getPerformative()){
                case ACLMessage.AGREE -> {
                    accord = true;
                    monAgent.println("accord trouve avec l autre sur %.2f".formatted(Double.valueOf(msg.getContent())));}
                case ACLMessage.REFUSE -> rejet = true;
                case ACLMessage.FAILURE -> rejet = true;
            }
            if(!accord && !rejet) {
                // 【正常还价流程】
                step++;
                offreAutre = Double.parseDouble(msg.getContent());
                monAgent.println("j'ai reçu une offre à %.2f".formatted(offreAutre));
                // 【同意条件】对方出价 <= 我方上次出价 = 对方开始让步
                if (offreAutre <= offrePrecedente) accord = true;
                // 【拒绝条件】对方出价 > 我方上限
                if (offreAutre > monAgent.seuil) rejet = true;
                // 【还价】既不同意也不拒绝时
                if (!accord && !rejet) {
                    // 【首次出价】从买方的理想价开始
                    if (offrePrecedente == 0) offrePrecedente = monAgent.prixSouhaite;
                    // 【涨 5%】
                    offre = offrePrecedente * (1 + epsilon);
                    offrePrecedente = offre;
                    var reponse = msg.createReply();
                    reponse.setContent(String.valueOf(offre));
                    myAgent.send(reponse);
                    monAgent.println("je propose %.2f".formatted(offre));
                }
            }
            // 【同意的回复】
            if(accord){
                var reponse = msg.createReply();
                reponse.setContent(String.valueOf(offreAutre));
                reponse.setPerformative(ACLMessage.AGREE);
                myAgent.send(reponse);
                monAgent.println("j'envoie mon accord");
            }
            // 【拒绝的回复】
            if(rejet){
                var reponse = msg.createReply();
                reponse.setPerformative(ACLMessage.REFUSE);
                myAgent.send(reponse);
            }
            // 【10 轮超时】发 FAILURE
            if(step == 10){
                var reponse = msg.createReply();
                reponse.setPerformative(ACLMessage.FAILURE);
                myAgent.send(reponse);
            }
        } else block();
    }

    @Override
    public boolean done() {
        if (accord)
            monAgent.println("Fin sur un accord ");
        if (rejet)
            monAgent.println("Fin sur un rejet ");
        if (step == 10)
            monAgent.println("Temps de négociation expiré");
        // 【三种结束条件之一】
        return step == 10 || accord || rejet;
    }
}
