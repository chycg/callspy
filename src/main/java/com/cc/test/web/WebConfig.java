package com.cc.test.web;

import java.util.Set;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.cc.test.util.ArrayUtils;

import lombok.Getter;

@Configuration
public class WebConfig {

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	@Getter
	@Value("${test.assist.yapi.domain:}")
	private String yapiDomain;

	@Value("${test.assist.package.prefix:}")
	private String packagePrefix;

	@Getter
	@Value("${test.assist.pre.url}")
	private String preUrl;

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	@Bean
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(url);
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUsername(username); // 用户名
		dataSource.setPassword(password); // 密码
		return dataSource;
	}

	/**
	 * 支持下划线映射
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean("sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource());
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setCacheEnabled(false);
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mybatis/mapper/*.xml"));
		sqlSessionFactory.setConfiguration(configuration);
		return sqlSessionFactory.getObject();
	}

	public boolean isPrefixed(String className) {
		Set<String> set = ArrayUtils.splitStr(packagePrefix);
		return set.stream().anyMatch(e -> className.startsWith(e));
	}

}
