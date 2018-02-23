package rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rest.models.Transaction;
import rest.models.TransactionsStatistics;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestTest {
	
	private final String ADD_TRANSACTION = "/transactions";
	private final String GET_STATS = "/statistics";
	
	private static ObjectMapper JSON;
	private static Transaction[] transactions;
	
	private static long MINUTE = 60*1000;
	
	@Autowired
    private MockMvc mvc;
	
	@BeforeClass
	public static void init() {
		JSON = new ObjectMapper();
		
		long now = new Date().getTime();
		
		transactions = new Transaction[] {
				new Transaction(132.7d, now - 9124), //valid
				new Transaction(192.4d, now - 91203), //old, invalid
				new Transaction(219.3d, now + 55034), //future, invalid
				new Transaction(319.3d, now + 2034), //close future, valid (request duration should be enough)
				new Transaction(128.9d, now), //valid
				new Transaction(294.0d, now - MINUTE - 1), //invalid, will be removed 1ms after now (way too soon)
				new Transaction(270.2d, now - 58*1000) //invalid, will be remove in 2s (request duration should be enough)
		};
	}
	
	@Test
	public void testAddTransaction() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(ADD_TRANSACTION)
					.contentType(MediaType.APPLICATION_JSON)
					.content(JSON.writeValueAsString(transactions[0]))
				)
		.andExpect(status().isCreated());
	}
	
	@Test
	public void testAddInvalidTransaction() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(ADD_TRANSACTION)
					.contentType(MediaType.APPLICATION_JSON)
					.content(JSON.writeValueAsString(transactions[1]))
				)
		.andExpect(status().isNoContent());
	}
	
	@Test
	public void testAddNullTransaction() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(ADD_TRANSACTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON.writeValueAsString(null))
				)
		.andExpect(status().isBadRequest());
	}
	
	
	@Test
	public void testGetStatistics() throws Exception {
		//register all transactions
		for(int i=0; i<transactions.length; ++i) {
			final boolean  isFinal = (i == transactions.length - 1);
			
			mvc.perform(MockMvcRequestBuilders
				.post(ADD_TRANSACTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON.writeValueAsString(transactions[i])))
			
				.andDo(new ResultHandler() {
					@Override
					public void handle(MvcResult arg0) throws Exception {
						if(isFinal)
							askForStatistics();
					}
				});
		}
	}
	
	private void askForStatistics() throws JsonProcessingException, Exception {
		Thread.sleep(500); //half a second to trigger statistics update
		
		mvc.perform(MockMvcRequestBuilders.get(GET_STATS).contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().json(JSON.writeValueAsString(getStats())));
	}
	
	private TransactionsStatistics getStats() {
		TransactionsStatistics stats = new TransactionsStatistics();
		
		long now = new Date().getTime();
		
		double sum = 0d;
		double max = 0d;
		double min = Double.MAX_VALUE;
		long count = 0l;
		
		for(Transaction tr : transactions) {
			//process only valid transaction
			if(now - tr.getTimestamp() < MINUTE && tr.getTimestamp() <= now) {
				double amount = tr.getAmount();
				
				sum += amount;
				max = Math.max(max, amount);
				min = Math.min(min, amount);
				
				++count;
			}
		}
		
		stats.update(sum, max, min, count);
		
		return stats;
	}
}
