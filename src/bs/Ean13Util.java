package bs;

public class Ean13Util {
	
	public static int mm2px(int n) {
		int r = Math.round(((float)  (1.0f/0.35278f)*((float) n)));
		return r;
	}
	
	public static final int checkDigit(int[] ean) {
		if (ean.length != 13) return 0;
		int sum = 0, cnt = ean.length-(1+1);
		while (cnt >= 0) {
			sum += (cnt%2==0 ? ean[cnt] : ean[cnt]*3);
			cnt--;
		}
		int checkDigigt = sum%10;
		return (checkDigigt == 0 ? 0 : (10-checkDigigt));
	}
	
}
