import java.math.BigInteger;
import java.util.*;

public class CANcode {
	
	private String identifier;
	private String extended;
	private String rtr;
	private ArrayList<datafield> datafields = new ArrayList<datafield>();
	private String description;
	private boolean showIdentifier;
	private boolean showData;
	private int numFields;
	
	public CANcode(String[] recieved){	
		this.identifier = recieved[0];
		this.rtr = recieved[1];
		this.extended = recieved[2];
		this.datafields.add(new datafield(recieved[3], recieved[4], recieved[5]));
		this.description = null;
		this.showIdentifier = true;
		this.showData = true;
		this.numFields = 1;
	}
	
	public void addData(String[] data){ //data[0] = length, data[1] = data field, data[2] = error code.

		BigInteger datavalue = new BigInteger(data[1].replaceAll("\\s",""),16);
		for(int i = 0; i < this.datafields.size(); i++){
			BigInteger temp = new BigInteger(this.datafields.get(i).getData().replaceAll("\\s",""),16);
			if(datavalue.compareTo(temp)<0){                        //if datavalue < temp
				this.datafields.add(i, new datafield(data[0], data[1], data[2]));
				this.numFields++;
				return;
			}
			else if(datavalue.compareTo(temp) == 0){
				this.datafields.get(i).recordOccurence(data[2]);
				return;
			}
		}
		this.datafields.add(new datafield(data[0], data[1], data[2]));
		this.numFields++;
	}
	
	public void giveDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public void hideData(){
		this.showData = false;
	}
	
	public void showData(){
		this.showData = true;
	}
	
	public void hideIdentifier(){
		this.showIdentifier = false;
	}
	
	public void showIdentifier(){
		this.showIdentifier = true;
	}
	
	public String getIdentifier(){
		return this.identifier;
	}
	
	public int getRowNumber(){
		if(this.showIdentifier){
			if(this.showData){
				return this.numFields;
			}
			else{return 1;}
		}
		else{return 0;}
	}
	
	public String toString(String mode){
		StringBuilder result = new StringBuilder("");
		if(this.showIdentifier | mode.equals("save")){
			if(description != null){
				result.append(description);
			}

			if(this.showData | mode.equals("save")){
				result.append(String.format("   %-20s:    %-23s\n",this.identifier,this.datafields.get(0).toString()));
				for(int i = 1; i <= this.datafields.size()-1; i++){
					result.append(String.format("   %-20s:    %-23s\n","   ",this.datafields.get(i).toString()));
				}
			}
			else{
				result.append(String.format("   %-20s:    %-23s\n",this.identifier,"data not displayed"));
			}
			return result.toString() + "\n";
		}
		return "";
	}
}
