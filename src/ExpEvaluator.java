import java.util.Scanner;
import java.util.Stack;

class MathStack{
	Stack<Character> exps;
	Stack<Double> nums;
	int e, n;
	
	MathStack(){
		exps = new Stack<Character>();
		nums = new Stack<Double>();
		e = -1;
		n = -1;
	}
	public void pushExp(char exp){
		exps.push(exp);
		e++;
	}
	public void pushNum(double num){
		//System.out.println(num);
		nums.push(Double.valueOf(num));
		n++;
	}
	
	public char popExp() {
		e--;
		return exps.pop();
	}
	public Double popNum() {
		n--;
		return nums.pop();
	}
	public boolean isEmptyExp() {
		return e == -1 ? true : false;
	}
	public boolean isEmptyNum() {
		return n == -1 ? true : false;
	}
	public char peek() {
		return exps.peek();
	}
	public void showN() {
		System.out.println(nums.toString());
		System.out.println(exps.toString());
	}
	
	public void flush() {
		//this.showN();
		while(! exps.empty() && exps.peek() != '(') {
			double n2 = this.popNum();
			double n1 = this.popNum();
			char exp = this.popExp();
			/*
			
			System.out.print(n1);
			System.out.print(exp);
			System.out.print(n2);
			System.out.print('=');*/
			
			double res = ExpEvaluator.evaluate(n1, n2, exp);
			this.pushNum(res);
			//System.out.println("["+res+"]");
		}
		if(! exps.empty()&& exps.peek() == '(') {
			exps.pop();
		}
	}
	public void flushPriority(int priority) {
		//this.showN();
		//System.out.println(priority);
		while(!exps.empty() && ExpEvaluator.priority(exps.peek()) >= priority && exps.peek() != '(' ) {
			double n2 = this.popNum();
			double n1 = this.popNum();
			char exp = this.popExp();
			
			
			/*System.out.print(n1);
			System.out.print(exp);
			System.out.print(n2);*/
			//System.out.print('=');
			
			double res = ExpEvaluator.evaluate(n1, n2, exp);
			this.pushNum(res);
			//System.out.println("["+res+"]");
		}
	}
}




public class ExpEvaluator {
	static boolean isExp(char c) {
		if(c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ) {
			return true;
		}
		return false;
	}
	
	static boolean isBracketL(char c) {
		if (c == '(') {
			return true;
		}
		return false;
	}
	
	static boolean isBracketR(char c) {
		if(c == ')') {
			return true;
		}
		return false;
	}
	
	public static int priority(char c) {
		if(c == '+' || c == '-') {
			return 0;
		}
		else if(c == '*' || c == '/' || c == '%') {
			return 1;
		}
		else if(c == '(' || c == ')') {
			return 2;
		}
		return -1;
	}
	
	public static double evaluate(double n1, double n2, char exp) {
		switch (exp) {
		case '+':
			return n1 + n2;
		case '-':
			return n1 - n2;
		case '*':
			return n1 * n2;
		case '/': 
			return n1 / n2;
		case '%':
			return n1 % n2;
		}
		return -7770.0;
	}
	

	
	public static double analyse (String term) {
		int i = 0;
		int start;
		int flag = 0;
		int prevExp = 0;
		MathStack stack = new MathStack();
		
		//System.out.println("<" + term + ">");
		
		for(; i < term.length(); i++) {
			//System.out.println(term.charAt(i));
			//stack.showN();
			if(isExp(term.charAt(i)) == true) {
				if(stack.isEmptyExp() && stack.isEmptyNum() && term.charAt(i) == '-') {
					flag = 1;
				}
				else if(prevExp == 0) {
					int priority = priority(term.charAt(i));
					stack.flushPriority(priority);
					stack.pushExp(term.charAt(i));
					prevExp = 1;
				}
				else {
					flag = 1;
					prevExp = 0;
				}
				
			}
			else if(isBracketL(term.charAt(i))) {
				stack.pushExp(term.charAt(i));
				prevExp = 0;
			}
			else if (isBracketR(term.charAt(i))) {
				stack.flush();
                prevExp = 0;
			}
			
			else if(Character.isDigit(term.charAt(i))){
				start = i;
				while(i < term.length() && (Character.isDigit(term.charAt(i) ) || term.charAt(i) == '.')) {
					i++;
				}
				if (flag == 1) {
					stack.pushNum(-1 * Double.valueOf(term.substring(start, i)));
					flag = 0;
				}
				else {
					stack.pushNum(Double.valueOf(term.substring(start, i)));
				}
				i--;
				prevExp = 0;
			}
			
		}
		//stack.showN();
		stack.flush();
		
		return stack.popNum();
	}
	
	/*
	public static void main(String[] args) {
		
		Scanner in = new Scanner(System.in);
		double result;
		String next = in.nextLine();
		
		result = analyse(next);
		System.out.println((int) result);
		
		
		
	}*/
}

