package issia23.data;

import java.io.Serializable;
import java.util.Random;

/**
 * 【产品类型枚举】咖啡机、洗衣机、鼠标、吸尘器、洗碗机等。
 * 每个类型有"规格数"和"标准价格"两个属性——用于生成具体产品时的价格浮动基准。
 * 实现 Serializable 以便通过 ACL 消息传输。
 */
public enum ProductType implements Serializable {
    Cafetiere( 3, 40d),
    LaveLinge( 3,  200d),
    SourisOrdi( 2,  40d),
    Aspirateur( 3,  100d),
    LaveVaisselle( 4,  200d);

    public double getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(double standardPrice) {
        this.standardPrice = standardPrice;
    }

    public int getNbParts() {
        return nbParts;
    }

    public void setNbParts(int nbParts) {
        this.nbParts = nbParts;
    }

    double standardPrice;
    int nbParts;

    private final Random rand = new Random();

    ProductType(  int nbParts,  double standardPrice){
        this.nbParts = nbParts;
        this.standardPrice = standardPrice;
    }

/*    @Override
    public String toString() {
        return String.format("%s,  difficulty=%d, transportable=%b, danger=%b " +
                "\n\tnb smallParts=%d\n\tnb bigParts=%d",
                this.name(), difficulty, transportable, danger, nbSmallParts, nbBigParts);
    }
*/



}
