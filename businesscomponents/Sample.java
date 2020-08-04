package businesscomponents;

import org.apache.commons.lang3.StringUtils;

public class Sample {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//String a="get help";
//String b="alexa ask ally testing get help";
//System.out.println(StringUtils.getJaroWinklerDistance(a, b));
//
//if(StringUtils.getJaroWinklerDistance(a, b)>0.40)
//{
//	System.out.println("success");
//}
//else
//	System.out.println("failure");
		
		String s = "hello bye use";
		String words[] = s.split(" ");
		String firstTwo = words[0] + "  " + words[1]; // first two words
		String lastOne = words[words.length - 1];
		System.out.println(lastOne);
	}

		
}
