// datafield
// Anders Helbo
// Morten Ambrosius

public class datafield {

	private String length;
	private String data;
	private int repetitions;
	private int faults;
	
	public datafield(String length, String data, String error) {
		
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
		String result = String.format("%-5s %-30s %5d  %5d", this.length, this.data, this.repetitions, this.faults);
		return result;
	}
	
	public void recordOccurence(String error){
		this.repetitions++;
		if(!error.equals("5")){
			this.faults++;
		}
	}
}
