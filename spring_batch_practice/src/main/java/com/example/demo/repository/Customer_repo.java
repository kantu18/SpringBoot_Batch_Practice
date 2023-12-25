package com.example.demo.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Customer_entity;

@Repository
public interface Customer_repo extends JpaRepository<Customer_entity, Serializable>{

}
