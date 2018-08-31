/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package app.myoss.cloud.mybatis.spring.boot.autoconfigure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.io.VFS;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Spring Boot VFS
 *
 * @author Hans Westerbeek
 * @author Eddú Meléndez
 * @author Kazuki Shimizu
 * @author Jerry.Chen
 * @since 2018年4月11日 上午11:07:07
 */
public class SpringBootVFS extends VFS {
    private final ResourcePatternResolver resourceResolver;

    /**
     * 初始化 Spring Boot VFS
     */
    public SpringBootVFS() {
        this.resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected List<String> list(URL url, String path) throws IOException {
        Resource[] resources = resourceResolver.getResources("classpath*:" + path + "/**/*.class");
        return Stream.of(resources).map(resource -> preserveSubpackageName(resource, path)).collect(
                Collectors.toList());
    }

    private static String preserveSubpackageName(final Resource resource, final String rootPath) {
        try {
            final String uriStr = resource.getURI().toString();
            final int start = uriStr.indexOf(rootPath);
            return uriStr.substring(start);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
