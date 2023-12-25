package com.example.demo.config;

import org.springframework.batch.item.ItemProcessor;

import com.example.demo.entity.Customer_entity;

public class Customer_Processor implements ItemProcessor<Customer_entity, Customer_entity>{

	@Override
	public Customer_entity process(Customer_entity item) throws Exception {
		
		if(item.getCountry().equals("China"))
		{
			item.setCountry("JAPAN");
		}
		return item;
	}

}
