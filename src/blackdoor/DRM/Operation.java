package blackdoor.DRM;

import java.io.Serializable;

public interface Operation extends Serializable {
	/**
	 * execute this operation on dataStructure
	 * @param dataStructure
	 * @return true if operation was successful
	 */
	public <T> boolean execute(T dataStructure);
	
	
}
