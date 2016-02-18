package rs.proxy.service;

public class RemoveServiceImpl implements IRemoveService {

	public int fuseki(String pid) {
		return RestServiceImpl.removeDataOfDriverFromFuseki(pid);
	}
}