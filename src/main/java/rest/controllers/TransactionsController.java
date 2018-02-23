package rest.controllers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import rest.models.BasicResponse;
import rest.models.Transaction;
import rest.models.TransactionsStatistics;
import rest.services.TransactionsStatisticsService;

@RestController
@RequestMapping(path="/")
public class TransactionsController {
	
	private TransactionsStatisticsService statisticsService;
	
	@PostConstruct
	public void init() {
		/*
		 * create a statistics service,
		 * keeping transactions data for the last 60 seconds
		 * and updating the statistics every half a second
		 */
		this.statisticsService = new TransactionsStatisticsService(60*1000, 500);
		this.statisticsService.execute();
	}
	
	@PreDestroy
	public void destroy() {
		//clean up
		this.statisticsService.stop();
	}
	
	
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	public @ResponseBody BasicResponse exceptionHandler(Exception e) {
		return new BasicResponse(e.getMessage());
	}
	
	
	
	@PostMapping(path="/transactions")
	public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) throws Exception {
		if(transaction == null)
			throw new Exception("Missing mandatory parameter.");
		
		boolean isAdded = this.statisticsService.add(transaction);
		
		return new ResponseEntity<String>(isAdded ? HttpStatus.CREATED : HttpStatus.NO_CONTENT) ;
	}
	
	@GetMapping(path="/statistics")
	public TransactionsStatistics getStatistics() {
		return this.statisticsService.getStatistics();
	}

}
