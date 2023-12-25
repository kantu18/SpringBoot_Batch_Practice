package com.example.demo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.entity.Customer_entity;
import com.example.demo.repository.Customer_repo;

@Configuration
public class Customer_Job {

	@Autowired
	private Customer_repo customer_repo;
	
	
	//Read the csv file
	@Bean
	public FlatFileItemReader<Customer_entity> itemreader()
	{
		//Reading the file through flatitemfilereader and setting path , name , 
		//skipping the first row and providing linemapper to separate by ',' and setting names
		
		FlatFileItemReader<Customer_entity> obj=new FlatFileItemReader<Customer_entity>();
		obj.setResource(new ClassPathResource("customers.csv"));
		obj.setName("customer_read");
		obj.setLinesToSkip(1);
		obj.setLineMapper(LineMapper());
		
		return obj;
	}

	private LineMapper<Customer_entity> LineMapper() {
		
		//Linemapper object to set the delimiter method to it.
		DefaultLineMapper<Customer_entity> defaultLineMapper=new DefaultLineMapper<Customer_entity>();
		
		//Setting the delimiter for the file read by separating through ',' and mapping the same name columns as the file.
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");
		
		//Conevrting the linemapper to object of customer type
		BeanWrapperFieldSetMapper<Customer_entity> fieldSetMapper= new BeanWrapperFieldSetMapper<Customer_entity>();
		fieldSetMapper.setTargetType(Customer_entity.class);
		
		defaultLineMapper.setLineTokenizer(lineTokenizer);
		defaultLineMapper.setFieldSetMapper(fieldSetMapper);
		
		return defaultLineMapper;
	}
	
	//Processor method using the Itemprocessor interface by implementing it into other class for logic building
	@Bean
	public Customer_Processor custom()
	{
		return new Customer_Processor();
	}
	
	//Writer method using RepositoryItemwriter
	@Bean
	public RepositoryItemWriter<Customer_entity> itemWriter()
	{
		RepositoryItemWriter<Customer_entity> itemWriterobj=new RepositoryItemWriter<Customer_entity>();
		itemWriterobj.setRepository(customer_repo);
		itemWriterobj.setMethodName("save");
		return itemWriterobj;
	}
	
	@Bean
	public Step steps(JobRepository jobRepository, PlatformTransactionManager manager)
	{
		return new StepBuilder("customer-step", jobRepository).
				<Customer_entity, Customer_entity>chunk(10,manager)
				.reader(itemreader()).processor(custom())
				.writer(itemWriter()).taskExecutor(taskExecutor()).build();
	}
	
	@Bean
	public Job runjob(JobRepository jobRepository,PlatformTransactionManager manager)
	{
		return new JobBuilder("customer-job", jobRepository).flow(steps(jobRepository,manager)).end().build();
	}
	
	@Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
}
