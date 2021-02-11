package com.sixthday.navigation;

import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.sixthday.sixthdayLogging.EventType.API;
import static com.sixthday.sixthdayLogging.OperationType.*;
import static com.sixthday.sixthdayLogging.logOperation;

@RestController
@Slf4j
public class LeftNavUtilityController {
    private static final String SUCCESS = "Success";
    private LeftNavTreeProcessor leftNavTreeProcessor;
    private DesignerIndexProcessor designerIndexProcessor;
    private LeftNavRepository leftNavRepository;

    @Autowired
    public LeftNavUtilityController(LeftNavTreeProcessor leftNavTreeProcessor, DesignerIndexProcessor designerIndexProcessor
            , LeftNavRepository leftNavRepository) {
        this.leftNavTreeProcessor = leftNavTreeProcessor;
        this.designerIndexProcessor = designerIndexProcessor;
        this.leftNavRepository = leftNavRepository;
    }

    @GetMapping(value = "/pendingNodes")
    public Map<String, Set<String>> pendingNodes() {
        return logOperation(log, API, GET_PENDING_NODES, () ->
                leftNavTreeProcessor.getRetryNodes()
        );
    }

    @GetMapping(value = "/reprocessPendingNodes")
    public String reprocessPendingNodes() {
        return logOperation(log, API, PROCESS_PENDING_NODES, () -> {
            leftNavTreeProcessor.reprocessPendingNodes();
            return SUCCESS;
        });
    }

    @GetMapping(value = "/buildLeftNav")
    public String buildLeftNav() {
        return logOperation(log, API, BUILD_LEFTNAV, () -> {
            leftNavTreeProcessor.buildLeftNav();
            return SUCCESS;
        });
    }

    @GetMapping(value = "/buildLeftNav/{categoryId}")
    public String buildLeftNavForACategory(@PathVariable String categoryId) {
        return logOperation(log, API, BUILD_LEFTNAV, () -> {
            leftNavTreeProcessor.startByEvent(categoryId, true);
            return SUCCESS;
        });
    }

    @GetMapping(value = "/getLeftNavFromEC/{path}")
    public LeftNavDocument getLeftNavFromEC(@PathVariable String path) {
        return logOperation(log, API, GET_LEFTNAV, () ->
            leftNavRepository.getLeftNavById(path)
        );
    }

    @GetMapping(value = "/buildDesignerIndex")
    public String buildDesignerIndex() {
        return logOperation(log, API, BUILD_DESIGNER_INDEX, () -> {
            designerIndexProcessor.buildDesignerIndex();
            return SUCCESS;
        });
    }

    @GetMapping(value = "/getDesignerIndex/{categoryId}")
    public DesignerIndex getDesignerIndex(@PathVariable String categoryId) {
        return logOperation(log, API, GET_DESIGNER_INDEX, () ->
                designerIndexProcessor.getDesignerIndex(categoryId)
        );
    }

    @GetMapping(value = "/getCategory/{categoryId}")
    public Optional<CategoryDocument> getCategory(@PathVariable String categoryId) {
        return logOperation(log, API, GET_CATEGORY, () ->
                leftNavTreeProcessor.getCategoryDocument(categoryId)
        );
    }
}
