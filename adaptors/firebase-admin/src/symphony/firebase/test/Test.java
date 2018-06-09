//package symphony.firebase.test;
//
//import com.google.firebase.FirebaseOptions;
//
//public class Test {
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.println("test");
//		FirebaseOptions options = new FirebaseOptions.Builder().build();
//		System.out.println("test end");
//	}
//
//}


package symphony.firebase.test;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args) {
		System.out.println("test");
		//Firebase firebase = new Firebase();
		//instantiate value event listener to the /symphony database path
		//firebase.connect("/symphony/bahay");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
