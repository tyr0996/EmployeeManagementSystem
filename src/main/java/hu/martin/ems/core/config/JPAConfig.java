package hu.martin.ems.core.config;


import hu.martin.ems.core.repository.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "hu.martin.ems", repositoryBaseClass = BaseRepositoryImpl.class)
public class JPAConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String jdbcUsername;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${spring.jpa.database-platform}")
    private String hibernateDialect;

    @Value("${entity_manager.packages.to.scan}")
    private String entityManagerPackagesToScan;

    @Value("${hibernate.pool_size}")
    private Integer hibernatePoolSize;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String dllAuto;

    @Value("{$spring.jpa.properties.hibernate.format_sql}")
    private String formatSql;

    @Value("{$spring.jpa.properties.hibernate.use_sql_comments}")
    private String useSqlComments;

    private static EntityManager entityManager;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(jdbcUsername)
                .password(jdbcPassword)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public EntityManager em() {
        return entityManagerFactory().getNativeEntityManagerFactory().createEntityManager();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[]{"hu.martin.ems"});
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", hibernateDialect);
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", dllAuto);
        jpaProperties.setProperty("hibernate.show_sql", hibernateShowSql);
        jpaProperties.setProperty("spring.jpa.properties.hibernate.format_sql", formatSql);
        jpaProperties.setProperty("spring.jpa.properties.hibernate.use_sql_comments", useSqlComments);
        em.setJpaProperties(jpaProperties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}