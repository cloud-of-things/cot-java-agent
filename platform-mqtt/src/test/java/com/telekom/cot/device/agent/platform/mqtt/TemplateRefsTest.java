package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hamcrest.Matchers;
import org.junit.Test;

public class TemplateRefsTest {

//    @Test
//    public void testTemplateRefsInMapAsKey() {
//        Map<List<TemplateId>, AtomicBoolean> map = new HashMap<>();
//        map.put(TemplateRefs.STATUS_OF_OPERATION_PUT_REQ_REFS.getResponseRefs(), new AtomicBoolean(false));
//        // check
//        assertThat(map.keySet().contains(TemplateRefs.STATUS_OF_OPERATION_PUT_REQ_REFS.getResponseRefs()), Matchers.equalTo(true));
//    }
//
//    @Test
//    public void test_STATUS_OF_OPERATION_PUT_REQ_REFS() {
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_PUT_REQ_REFS.getRequest(), Matchers
//                        .equalTo(TemplateId.STATUS_OF_OPERATION_PUT_REQ));
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_PUT_REQ_REFS.getResponseRefs().get(0), Matchers
//                        .equalTo(TemplateId.OPERATION_RESTART_RES));
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_PUT_REQ_REFS.getResponseRefs().get(1), Matchers
//                        .equalTo(TemplateId.OPERATION_TEST_RES));
//    }
//
//    @Test
//    public void test_STATUS_OF_OPERATION_GET_REQ_REFS() {
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_GET_REQ_REFS.getRequest(), Matchers
//                        .equalTo(TemplateId.STATUS_OF_OPERATION_GET_REQ));
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_GET_REQ_REFS.getResponseRefs().get(0), Matchers
//                        .equalTo(TemplateId.STATUS_OF_OPERATION_RESTART_RES));
//        assertThat(TemplateRefs.STATUS_OF_OPERATION_GET_REQ_REFS.getResponseRefs().get(1), Matchers
//                        .equalTo(TemplateId.STATUS_OF_OPERATION_TEST_RES));
//    }
}
