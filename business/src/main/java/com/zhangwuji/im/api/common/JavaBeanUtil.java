package com.zhangwuji.im.api.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import com.zhangwuji.im.api.entity.GeoBean;
import org.springframework.stereotype.Component;

@Component
public class JavaBeanUtil {

    public List<GeoBean> sublist(List<GeoBean> clubs,int pageIndex,int pageSize)
    {
        List<GeoBean> geoBeanList =new LinkedList<>();
        int currIdx = (pageIndex > 1 ? (pageIndex -1) * pageSize : 0);
        for (int i = 0; i < pageSize && i < clubs.size() - currIdx; i++) {
            GeoBean geoBean = clubs.get(currIdx + i);
            geoBeanList.add(geoBean);
        }
        return  geoBeanList;
    }

    /**
     * 使用 Map按value进行排序
     *
     * @param
     * @return
     */
    public Map<Object, Object> sortMapByValue(Map<Object, Object> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<Object, Object> sortedMap = new LinkedHashMap<Object, Object>();
        List<Map.Entry<Object, Object>> entryList = new ArrayList<Map.Entry<Object, Object>>(oriMap.entrySet());

        Collections.sort(entryList, new MapValueComparator());

        Iterator<Map.Entry<Object, Object>> iter = entryList.iterator();
        Map.Entry<Object, Object> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public class MapValueComparator implements Comparator<Map.Entry<Object, Object>> {
        @Override
        public int compare(Entry<Object, Object> me1, Entry<Object, Object> me2) {
            return me1.getValue().toString().compareTo(me2.getValue().toString());
        }
    }

    public Object getFirstKeyFromMap(Map<Object, Object> map) {
        Object obj = null;
        for (Entry<Object, Object> entry : map.entrySet()) {
            obj = entry.getKey();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    public static Map<String, Object> convertBeanToMap(Object bean) {
        try {
            Class type = bean.getClass();
            Map<String, Object> returnMap = new HashMap<String, Object>();
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
                if (!propertyName.equals("class")) {
                    Method readMethod = descriptor.getReadMethod();
                    Object result = readMethod.invoke(bean, new Object[0]);
                    if (result != null) {
                        returnMap.put(propertyName, result);
                    } else {
                        returnMap.put(propertyName, "");
                    }
                }
            }
            return returnMap;
        }catch (Exception ee){return null;}
    }
}
