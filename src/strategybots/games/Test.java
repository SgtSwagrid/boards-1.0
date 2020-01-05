package strategybots.games;

import strategybots.games.A.B;

public class Test {
    public static void main(String[] args) {
        
        A a = new A();
        B b = a.new B();
        
        B b2 = b.clone();
        
        a.x = 1;
        b.y = 11;
        
        System.out.println(b2.getA().x);
        System.out.println(b2.y);
        
    }
}

class A implements Cloneable {
    
    int x = 0;
    
    class B implements Cloneable {
        
        int y = 10;
        
        @Override
        public B clone() {
            try {
                return (B) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public A getA() { return A.this; }
    }
}