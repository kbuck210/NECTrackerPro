package junit.test.dbstats;

import static org.junit.Assert.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.junit.Test;

import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Season;

public class TestPlayerStats {

	private SeasonService ss;
	private Season nec28;
	
	@Test
	public void testSeasonService() {
		EJBContainer container = EJBContainer.createEJBContainer();
		try {
			ss = (SeasonService) container.getContext().lookup("java:global/classes/SeasonService");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		assertNotNull(ss);
	}
	
	@Test
	public void testNec28() {
		assertNotNull(ss);
		
		nec28 = ss.selectById(28);
		assertNotNull(nec28);
	}
}
