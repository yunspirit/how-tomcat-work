package AAA00LeetCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    public int minMoves2(int[] nums) {
        Arrays.sort(nums);
        int i = 0, j = nums.length-1;
        int res = 0;
        while (i < j){
            res += nums[j] - nums[i];
            i++;
            j--;
        }
        return res;
    }
    public int islandPerimeter(int[][] grid) {
        if(grid == null || grid.length == 0) return 0;
        int res = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if(grid[i][j]==0) continue;
                res+=judge(grid,i,j);
            }
        }
        return res;
    }
    public int judge(int[][]grid , int x , int y){

        int res=0;
        if(y-1<0 || grid[x][y-1]== 0) res+=1;
        if (y + 1 == grid[0].length || grid[x][y+1] ==0) res+=1;
        if(x-1<0 || grid[x-1][y]==0) res+=1;
        if(x+1==grid.length || grid[x+1][y]==0 ) res+=1;
        return res;
    }

//    public boolean repeatedSubstringPattern(String s) {
//        if(s==null || s.length()==0) return false;
//        int n=s.length();
//        int next[]=new int[n+1];
//        next[0] = -1;
//        int j=0,k=-1;
//        while (j<s.length()){
//            if( k == -1 || s.charAt(j)==s.charAt(k)){
//                ++k;
//                ++j;
//                next[j]=k;
//            }else {
//                k = next[k];
//            }
//        }
//        return next[n]>0 && next[n] % (n-next[n])==0 ;
//    }


    public boolean canIWin(int maxChoosableInteger, int desiredTotal) {
        Map<Integer,Boolean> map = new HashMap<>();
        if(maxChoosableInteger>=desiredTotal) return true;
        if((1+maxChoosableInteger)*maxChoosableInteger/2<desiredTotal) return false;
        return helpCanWin(maxChoosableInteger,desiredTotal,map,0);
    }
    public  boolean helpCanWin(int maxChoose, int total, Map<Integer,Boolean> map , int used){
        if(map.containsKey(used)) return map.get(used);
        for (int i = 0; i < maxChoose; i++) {
            int cur = 1<<i;
            if((cur & used)==0){
                if(i+1>=total || !helpCanWin(maxChoose,total-(i+1),map,used|cur)){
                    map.put(used,true);
                    return true;
                }
            }
        }
        map.put(used,false);
        return false;
    }

    public int findComplement(int num) {
//         负数不需要下列操作
//        正数需要下列操作
        int mask = Integer.highestOneBit(num) -1;
        return ~num & mask;
    }


    public int totalHammingDistance(int[] nums) {
        int n = nums.length;
        int sum = 0;
        for (int i = 0; i < 32; i++) {
            int tag = 0;
            for (int a : nums) {
                if(((a >> i)&1) == 1) tag++;
            }
            sum += tag * (n - tag);
        }
        return sum;
    }


}