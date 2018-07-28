package bm.jeep;

/**
 * Instantiates a basic RRP Response which contains only the RID and the success boolean
 * 
 * @author Carlo Miras
 */
public class ResBasic extends AbstResponse {

	/**
	 * Creates a basic response which only contains the RID, RTY, and success parameters
	 * 
	 * @param request The request sent to the BM that warrants this response
	 * @param success <b>True</b> if request processing was successful, <b>false</b> otherwise
	 */
	public ResBasic(AbstRequest request, boolean success) {
		super(request, success);
	}
}
