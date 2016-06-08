
public class datafield {

	private String length;
	private String data;
	private int repetitions;
	private int faults;
	
	public datafield(String length, String data, String error){
		
		this.length = length;
		this.data = data;
		this.repetitions = 1;
		if (error.equals("5")) {
			this.faults = 0;
		
		} else {
			
			this.faults = 1;
		}	
	}
	
	public String getData(){
		return this.data;
	}
	
	public String toString(){
		String result = String.format("%1s %-30s %4d  %4d", this.length, this.data, this.repetitions, this.faults);
		return result;
	}
	
	public void recordOccurence(String error){
		this.repetitions++;
		if(!error.equals("5")){
			this.faults++;
		}
	}
}
