package com.javadi.remotepartitioning.job.worker;

import com.javadi.remotepartitioning.config.ApplicationConstants;
import com.javadi.remotepartitioning.model.Customer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerProperties;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Profile("worker")
@Configuration
public class WorkerConfiguration {

    public final DataSource dataSource;
    private final RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory;

    public WorkerConfiguration(DataSource dataSource, RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory) {
        this.dataSource = dataSource;
        this.workerStepBuilderFactory = workerStepBuilderFactory;
    }

    @Bean
    public DirectChannel repliesFromWorkers() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundFlow(KafkaTemplate kafkaTemplate) {
        return IntegrationFlows
                .from(repliesFromWorkers())
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic("repliesFromWorkers"))
                .route("repliesFromWorkers")
                .get();
    }

    @Bean
    public DirectChannel requestForWorkers() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow inboundFlow(ConsumerFactory consumerFactory) {
        return IntegrationFlows
                .from(Kafka.inboundChannelAdapter(consumerFactory, new ConsumerProperties("requestForWorkers")))
                .channel(requestForWorkers())
                .get();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(10));
        return pollerMetadata;
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    @Bean
    public Step workerStep() {
        SimpleStepBuilder workerStepBuilder = workerStepBuilderFactory.get("workerStep")
                .inputChannel(requestForWorkers())
                .outputChannel(repliesFromWorkers())
                .<Customer, Customer>chunk(ApplicationConstants.WORKER_CHUNK_SIZE)
                .reader(pagingItemReader(null, null))
                .processor(itemProcessor())
                .writer(customerItemWriter());
        return workerStepBuilder.build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> pagingItemReader(@Value("#{stepExecutionContext['minValue']}") Long minValue,
                                                           @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        System.out.println("reading " + minValue + " to " + maxValue);
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(1000);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from CUSTOMER");
        queryProvider.setWhereClause("where id >= " + minValue + " and id < " + maxValue);

        Map<String, Order> sortKeys = new HashMap<>(1);

        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Customer, Customer> itemProcessor() {
        return item -> {
            Thread.sleep(ApplicationConstants.WORKER_PROCESSING_WAITE_TIME_MS);
            System.out.println(item);
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Customer> customerItemWriter() {
        return items -> {
            System.out.printf("%d items were written%n", items.size());
        };
    }

}
