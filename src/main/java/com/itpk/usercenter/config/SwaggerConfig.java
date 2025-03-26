package com.itpk.usercenter.config;

import com.itpk.usercenter.controller.UserController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //enable设置是否启动Swagger
                .enable(true)
                //通过.select()方法，去配置扫描接口
                .select()
                //RequestHandlerSelectors配置如何扫描接口
                .apis(RequestHandlerSelectors.basePackage("com.itpk.usercenter.controller"))
                // 配置如何通过path过滤,即这里只扫描请求以/koko开头的接口
                .paths(PathSelectors.any())
                .build();
    }
    //配置文档信息ff
    private ApiInfo apiInfo() {
       return new ApiInfoBuilder()
               .title("PK用户中心")
               .description("用户中心接口文档")
               .version("1.0.0")
               .build();
    }

}

