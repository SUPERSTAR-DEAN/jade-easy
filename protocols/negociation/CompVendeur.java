package protocols.negociation;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * 【卖方行为】讨价还价中卖方这一侧的决策逻辑。
 *
 * 判断规则：
 *   - `对方出价 >= 我方上次出价` → 同意（对方开始接受）
 *   - `对方出价 < 我方阈值(50)` → 拒绝（价格太低）
 *   - 否则 → 我方还价，价格在上次基础上降 10%
 *   - 达到 10 轮还没谈成 → 发 FAILURE 表示超时
 *
 * 【与 CompAcheteur 的关键差异】
 *   - 卖方 epsilon = 0.10（更大幅度让步），买方 epsilon = 0.05
 *   - 卖方构造时就把 prixSouhaite 存进 offrePrecedente，因为卖方是"发起方"
 *   - 卖方在算出新出价后，如果新出价 <= 对方出价就直接成交
 */
public class CompVendeur extends Behaviour {

    int step = 0;
    boolean accord;
    boolean rejet;
    double offreAutre, offrePrecedente, offre;
    // 【让步步长】0.1 = 每次降 10%
    double epsilon = 0.1;
    Negociateur monAgent;
    MessageTemplate modele;

    // 【构造】设定卖方阈值并把理想价作为首次出价
    CompVendeur(Negociateur monAgent, MessageTemplate modele) {
        super(monAgent);
        this.monAgent = monAgent;
        this.modele = modele;
        monAgent.seuil = 50;                    // 【卖方阈值】低于 50 就拒绝
        offrePrecedente = monAgent.prixSouhaite; // 【卖方的理想价 100 作为首次出价】
    }

    @Override
    public void action() {
        var msg = myAgent.receive(modele);
        if (msg != null) {
            // 【先看终态消息】
            switch (msg.getPerformative()){
                case ACLMessage.AGREE -> {
                        accord = true;
                        monAgent.println("accord trouve avec l autre sur " + msg.getContent());}
                case ACLMessage.REFUSE -> rejet = true;
                case ACLMessage.FAILURE -> rejet = true;
            }
            if (!accord && !rejet) {
                step++;
                offreAutre = Double.parseDouble(msg.getContent());
                monAgent.println("j'ai reçu une offre à %.2f".formatted(offreAutre));
                // 【同意条件】对方出价 >= 我方上次出价
                if (offreAutre >= offrePrecedente) accord = true;
                // 【拒绝条件】对方出价 < 我方下限
                if (offreAutre < monAgent.seuil) rejet = true;
                // 【还价】
                if (!accord && !rejet) {
                    // 【降 10%】
                    offre = offrePrecedente * (1 - epsilon);
                    // 【关键：如果降完的新价还低于对方出价，直接成交】
                    //   这是卖方"能松口就松口"的策略——只要对方出价高于新价，就接受
                    if (offre<offreAutre) {
                        offre = offreAutre;
                        accord = true;
                    }
                    else {
                        offrePrecedente = offre;
                        var reponse = msg.createReply();
                        reponse.setContent(String.valueOf(offre));
                        myAgent.send(reponse);
                        monAgent.println("je propose %.2f".formatted(offre));
                    }
                }
                if (accord) {
                    var reponse = msg.createReply();
                    reponse.setContent(String.valueOf(offreAutre));
                    reponse.setPerformative(ACLMessage.AGREE);
                    myAgent.send(reponse);
                }
                if (rejet) {
                    var reponse = msg.createReply();
                    reponse.setPerformative(ACLMessage.REFUSE);
                    myAgent.send(reponse);
                }
                if (step == 10) {
                    var reponse = msg.createReply();
                    reponse.setPerformative(ACLMessage.FAILURE);
                    myAgent.send(reponse);
                }
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
        return step == 10 || accord || rejet;
    }

}
