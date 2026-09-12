package issia23.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * class representing a product
 * - name
 * - specification
 * - price
 * This class create the list of products used un the simulation;
 * for each specification, new products are created with a variation of price regarding the standard price included in the specification
 *
 * 【产品类】循环经济场景中用户持有的产品（咖啡机、洗衣机等）。
 * 字段：name（名称）、type（产品类型，含规格数/标准价）、price（实际价格，相对标准价有浮动）
 * 实现 Serializable 以便通过 ACL 消息传输。
 * */
public class Product implements Serializable {
    /**name of the product*/
    String name;
    /**specification that indicate the nb of parts, its danger, standard price...*/
    ProductType type;
    /**price of the product*/
    double price;
    /**faulty part*/
    Part faultyPart;
    /**list of parts of the product*/
    public  List<Part> listParts;
    /**unique id of the product*/
    int id;
    /**nb of created products*/
    static int nbProducts = 0;
    /**list of  created products*/
    public static List<Product> listProducts;
    /**products have a price between standard price +/- VARIATION%*/
    public static final int VARIATION = 30;
    public static final int VARIATION_STEP = 10;
    Random rand = new Random();


    Product(String name, ProductType type){
        this.name = name;
        this.type = type;
        price = type.getStandardPrice();
        listParts = new ArrayList<>();
        for(int i=0; i<type.nbParts; i++)
            listParts.add(new Part(name+"-"+i, id*100+i, rand.nextInt(0,4), price/type.nbParts));
        id = ++nbProducts;
    }

    Product(String name, ProductType type, double price){
        this(name,type);
        this.price = price;
    }


    @Override
    public String toString() {
        return String.format("Produit{ %d : %s - %n%s - %n%.2f€}", id, name, type, price);
    }


    /**for each specification,
     * new products are created with a price from standard price - VARIATION% to standard price + VARIATION%
     * by step of VARIATIONSTEP% of the standard price.
     *  @return the list of created products  */
    static public List<Product> getListProducts() {
         if (listProducts == null) {
            listProducts = new ArrayList<>();
            var listeTypes = ProductType.values();
            for(var aType:listeTypes){
                for(int i=-VARIATION; i<=VARIATION; i+=VARIATION_STEP) {
                    listProducts.add(new Product(aType.toString()+i, aType, aType.getStandardPrice()*(1+i/100d)));
                }
            }
        }
        return listProducts;
    }

    /**choose randomly a part of the product that is identified as faulty*/
    public Part getFaultyPart() {
        if (Objects.isNull(faultyPart)) {
            var hasard = rand.nextInt(type.nbParts);
            faultyPart = listParts.get(hasard);
        }
        return faultyPart;
    }

        public String getName() {
        return name;
    }



    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price ;
    }

    public static void main(String[] args)
    {
        var tab = Product.getListProducts();
        for(var p:tab)  System.out.println(p);
        System.out.println("-".repeat(20));
    }
}
