package com.kimreporter.datasourcetest;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class DataSourceTest {
	
	@Inject
	private DataSource ds;

	@Test
	public void testConnection() throws Exception{
		try(Connection con=(Connection) ds.getConnection()) {
			System.out.println("CON >> ");
			System.out.println(con);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
