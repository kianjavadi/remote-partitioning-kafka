package com.javadi.remotepartitioning.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfiguration {

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final ApplicationContext applicationContext;

    public AppConfiguration(JobExplorer jobExplorer, JobRepository jobRepository, JobRegistry jobRegistry, ApplicationContext applicationContext) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobRegistry = jobRegistry;
        this.applicationContext = applicationContext;
    }

    @Bean
    public synchronized JobRegistryBeanPostProcessor getJobRegistryBeanPostProcessor() throws Exception {
        JobRegistryBeanPostProcessor jobRegistry = new JobRegistryBeanPostProcessor();
        jobRegistry.setJobRegistry(this.jobRegistry);
        jobRegistry.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        jobRegistry.afterPropertiesSet();
        return jobRegistry;
    }

    @Bean
    public JobOperator getJobOperator() throws Exception {
        SimpleJobOperator simpleJobOperator = new SimpleJobOperator();
        simpleJobOperator.setJobLauncher(getJobLauncher());
        simpleJobOperator.setJobParametersConverter(new DefaultJobParametersConverter());
        simpleJobOperator.setJobRepository(this.jobRepository);
        simpleJobOperator.setJobExplorer(this.jobExplorer);
        simpleJobOperator.setJobRegistry(this.jobRegistry);
        simpleJobOperator.afterPropertiesSet();
        return simpleJobOperator;
    }

    @Bean
    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(jobOperatorExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public ThreadPoolTaskExecutor jobOperatorExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(ApplicationConstants.JOB_OPERATOR_CORE_POOL_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(ApplicationConstants.JOB_OPERATOR_MAX_POOL_SIZE);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }

}
