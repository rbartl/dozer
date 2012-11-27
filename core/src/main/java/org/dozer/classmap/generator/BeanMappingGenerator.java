/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dozer.classmap.generator;

import org.dozer.classmap.ClassMap;
import org.dozer.classmap.ClassMapBuilder;
import org.dozer.classmap.Configuration;
import org.dozer.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
* @author Dmitry Spikhalskiy
*/
public class BeanMappingGenerator implements ClassMapBuilder.ClassMappingGenerator {

  static final List<BeanFieldsDetector> fieldDetectors = new ArrayList<BeanFieldsDetector>() {{
    add(new ProtobufBeanFieldsDetector());
    add(new JavaBeanFieldsDetector());
  }};


  public boolean accepts(ClassMap classMap) {
    return true;
  }

  public boolean apply(ClassMap classMap, Configuration configuration) {
    Class<?> srcClass = classMap.getSrcClassToMap();
    Class<?> destClass = classMap.getDestClassToMap();

    Set<String> destFieldNames = getAcceptsFieldsDetector(destClass).getWritableFieldNames(destClass);
    Set<String> srcFieldNames = getAcceptsFieldsDetector(srcClass).getReadableFieldNames(srcClass);
    Set<String> commonFieldNames = CollectionUtils.intersection(srcFieldNames, destFieldNames);

    for (String fieldName : commonFieldNames) {
      if (GeneratorUtils.shouldIgnoreField(fieldName, srcClass, destClass)) {
        continue;
      }

      // If field has already been accounted for, then skip
      if (classMap.getFieldMapUsingDest(fieldName) != null || classMap.getFieldMapUsingSrc(fieldName) != null) {
        continue;
      }

      GeneratorUtils.addGenericMapping(classMap, configuration, fieldName, fieldName);
    }
    return false;
  }

  private static BeanFieldsDetector getAcceptsFieldsDetector(Class<?> clazz) {
    for (BeanFieldsDetector detector : fieldDetectors) {
      if (detector.accepts(clazz)) return detector;
    }
    return null;
  }

  protected interface BeanFieldsDetector {
    boolean accepts(Class<?> clazz);
    Set<String> getReadableFieldNames(Class<?> clazz);
    Set<String> getWritableFieldNames(Class<?> clazz);
  }
}
