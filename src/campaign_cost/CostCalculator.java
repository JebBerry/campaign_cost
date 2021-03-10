package campaign_cost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CostCalculator {
	
	public static List<Integer> audienceSizeThresholds = new ArrayList<Integer>(Arrays.asList(5000, 15000, 25000, 50000));;
	
	//enum for campaign type
	public enum CampaignType {
		EMAIL("EM", 1500, new ArrayList<Double>(Arrays.asList(0.0, 0.66, 0.42, 0.27, 0.17))),
		SMS("SMS", 1800, new ArrayList<Double>(Arrays.asList(0.0, 1.1, 0.57, 0.41, 0.25))),
		DIRECT_MAIL("DM", 8000, new ArrayList<Double>(Arrays.asList(0.0, 1.56, 1.0, 0.7, 0.62)));
		
		public final String name;
		public final int cost;
		public final ArrayList<Double> costScale;

		CampaignType(String name, int cost, ArrayList<Double> costScale) {
			this.name = name;
			this.cost = cost;
			this.costScale = costScale;
		}
		
		//look up function to find enum given the abbreviation
		public static CampaignType getName(String abbreviation) {
			for (CampaignType type : CampaignType.values()) {
				if (type.name.equals(abbreviation)) {
					return type;
				}
			}
			return null;
		}
		
		public int getCost() {
			return cost;
		}
		
		public ArrayList<Double> getCostScale() {
			return costScale;
		}
	}
		
	public static void main(String[] args) {
		CostCalculator costCalc = new CostCalculator();
		Map<String, Object> input = new TreeMap<String, Object>();
		input = costCalc.readInputInput(args);
		if (input == null) {
			System.out.println("An error occured please try again.");
		}
		double cost = 0;
		for (CampaignType type : (List<CampaignType>)input.get("types")) {
			cost = cost + costCalc.calculateCost((int)input.get("size"), type);
		}
		if (cost >= 0 ) {
			System.out.println(String.format("campaign cost: %1$,.2f", cost));
		} else {
			System.out.println("An error occured please try again.");
		}
	}
	
	
	//returns a map containing the size and types of the campaign
	private Map<String, Object> readInputInput(String[] args) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result.put("size", Integer.parseInt(args[1]));
			List<CampaignType> types = new ArrayList<CampaignType>();
			for (String str : args[3].split("\\|")) {
				CampaignType typeFound = CampaignType.getName(str);
				if (typeFound != null) {
					types.add(typeFound);
				}
			}
			result.put("types", types);
		} catch (Throwable t) {
			System.out.println("There was an error in your input arguments.");
			return null;
		}
		return result;
	}
	
	//returns the cost of a campaign given the size and type
	private double calculateCost(int size, CampaignType type)  {
		double result = 0;
		int flatCost = 0;
		double dynamicCost = 0.0;
		switch (type) {
			case EMAIL: {
				flatCost = CampaignType.EMAIL.getCost();
				dynamicCost = calculateDynamicCost(audienceSizeThresholds, CampaignType.EMAIL.getCostScale(), size);
				break;
			}
			case SMS: {
				flatCost = CampaignType.SMS.getCost();
				dynamicCost = calculateDynamicCost(audienceSizeThresholds, CampaignType.SMS.getCostScale(), size);
				break;
			}
			case DIRECT_MAIL: {
				flatCost = CampaignType.DIRECT_MAIL.getCost();
				dynamicCost = calculateDynamicCost(audienceSizeThresholds, CampaignType.DIRECT_MAIL.getCostScale(), size);
				break;
			}
		}
		if(dynamicCost < 0) {
			System.out.println("The data provided is mis-configured.");
			System.out.println("Please make sure your groups are formated correctly.");
			return -1;
		}
		result = flatCost + dynamicCost;
		return result;
	}
	
	//returns the dynamic cost of a campaign given the size, threshhold groups, and cost groupings
	private double calculateDynamicCost(List<Integer> thresholds, List<Double> costs, int size) {
		if(thresholds.size() + 1 != costs.size()) {
			return -1;
		}
		double result = 0;
		if (size <= thresholds.get(0)) {
			result = costs.get(0) * size;
		} else if (size <= thresholds.get(1)) {
			result = (costs.get(0) * thresholds.get(0)) + (costs.get(1) * (size - thresholds.get(0)));
		} else if (size <= thresholds.get(2)) {
			result = (costs.get(0) * thresholds.get(0)) + (costs.get(1) * (thresholds.get(1) - thresholds.get(0))) + (costs.get(2) * (size - thresholds.get(1)));
		} else if (size <= thresholds.get(3)) {
			result = (costs.get(0) * thresholds.get(0)) + (costs.get(1) * (thresholds.get(1) - thresholds.get(0))) + (costs.get(2) * (thresholds.get(2) - thresholds.get(1))) + (costs.get(3) * (size - thresholds.get(2)));
		} else if (size < thresholds.get(3)){
			result = (costs.get(0) * thresholds.get(0)) + (costs.get(1) * (thresholds.get(1) - thresholds.get(0))) + (costs.get(2) * (thresholds.get(2) - thresholds.get(1))) + (costs.get(3) * (thresholds.get(3) - thresholds.get(2))) + (costs.get(4) * (size - thresholds.get(3)));
		}
		return result;
	}
}
