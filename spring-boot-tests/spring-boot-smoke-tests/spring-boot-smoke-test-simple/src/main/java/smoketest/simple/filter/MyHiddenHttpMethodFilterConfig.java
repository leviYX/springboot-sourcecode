package smoketest.simple.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
public class MyHiddenHttpMethodFilterConfig{

	@Bean
    public HiddenHttpMethodFilter myHiddenHttpMethodFilter(){
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		hiddenHttpMethodFilter.setMethodParam("cjb");
		return hiddenHttpMethodFilter;
    }

}
