package com.javadi.remotepartitioning.controller;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile("!worker")
@RequestMapping("/job")
@RestController
public class JobController {

	private final JobOperator jobOperator;

	public JobController(JobOperator jobOperator) {
		this.jobOperator = jobOperator;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public long launch(@RequestParam Integer minId, @RequestParam Integer maxId) throws Exception {
		return this.jobOperator.start("job", String.format("minId=%s,maxId=%s,date=%s", minId, maxId, System.currentTimeMillis()));
	}

}
