package springbook.user.service;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.util.PatternMatchUtils;

public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {
    public void setMappedClassName(String mappedClassName){
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    static class SimpleClassFilter implements ClassFilter {
        String mappedName;

        private SimpleClassFilter(String mappedName){
            this.mappedName = mappedName;
        }

        public boolean matches(Class<?> clazz){

            /**
             * Method: simpleMatch
             * - support wildcard which character '*'
             * e.g) *name, name*, *name*
             */
            return PatternMatchUtils.simpleMatch(mappedName,clazz.getSimpleName());
        }
    }

}
