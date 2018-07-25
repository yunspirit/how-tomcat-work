package AAA00LeetCode;

public class CircularArrayLoop {
    public boolean circularArrayLoop(int[] nums) {
        for (int i = 0; i <nums.length ; i++) {
            if (nums[i] == 0) {
                continue;
            }
            int j=i,k=getIndex(i,nums);
            while (nums[k]*nums[i]>0 && nums[getIndex(k,nums)]*nums[i]>0){
                if(k == j){
//                  one element loop
                    if(j==getIndex(j,nums)){
                         break;
                    }
                    return true;
                }
                j=getIndex(j,nums);
                k=getIndex(getIndex(k,nums),nums);
            }
//            路过的全部置为0
            j=i;
            int val = nums[i];
            while (nums[j] * val > 0) {
                int next = getIndex(j,nums);
                nums[j] = 0;
                j=next;
            }
        }
        return false;
    }
    int getIndex(int i,int[] nums){
        int index=(i+nums[i])%nums.length;
        if(index<0){
            index += nums.length;
        }
        return index;
    }
    public boolean repeatedSubstringPattern2(String s) {
        int mask=1,start=0;
        for (int i = 1; i <s.length() ; i++) {
            if(s.charAt(i) == s.charAt(0)){
                start=i;
                break;
            }
            mask|=1<<i;
        }

        for (int j = start ; j<s.length(); j++){

        }
        return false;
    }
//
//    public boolean repeatedSubstringPattern(String s) {
//        int n = s.length();
//        for (int i = 1; i <= n/2; i++) {
//            if(n % i == 0){
//                int c = n / i;
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < c ; j++) {
//                    sb.append(s.substring(0,i));
//                }
//                if(sb.toString().equals(s)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    boolean repeatedSubstringPattern(String str) {
        int i = 1, j = 0, n = str.length();
        int dp[] =new  int[str.length()+1];
        while( i < str.length() ){
            if( str.charAt(i) == str.charAt(j) ) dp[++i]=++j;
            else if( j == 0 ) i++;
            else j = dp[j];
        }
        System.out.println(dp[n]);
        return (dp[n] > 0) && (dp[n]%(n-dp[n])==0);
    }
    public static void main(String[] args) {
        String a="abaaba";
        new CircularArrayLoop().repeatedSubstringPattern(a);
    }
}
