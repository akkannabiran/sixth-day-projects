package com.sixthday.kafka.app.interceptors.util;

import com.carefirst.nexus.utils.ThreadLocalContextInfo;
import com.carefirst.nexus.utils.web.helpers.GuidHelper;
import com.carefirst.nexus.utils.web.model.NexusUtilsConstants;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;

@UtilityClass
public class GUIDUtil {
     public void initGuid() {
        String guid = (String) ThreadLocalContextInfo.getContextInfo(NexusUtilsConstants.GUID_CTX_ATTR_NAME);
        if (StringUtils.isEmpty(guid)) {
            guid = GuidHelper.createGuid();
            ThreadLocalContextInfo.create();
            ThreadLocalContextInfo.setContextInfo(NexusUtilsConstants.GUID_CTX_ATTR_NAME, guid);
            ThreadContext.put(NexusUtilsConstants.GUID_CTX_ATTR_NAME, guid);
        }
    }

    public void destroyGuid() {
        ThreadLocalContextInfo.destroy();
    }
}
