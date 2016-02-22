package rs.proxy.service;

public class RemoveServiceImpl implements IRemoveService {

	public String fuseki(String pid) {
		RestServiceImpl.removeDataOfDriverFromFuseki(pid);
		return "Deleted data from fuseki. Driver pid = " + pid;
	}
}