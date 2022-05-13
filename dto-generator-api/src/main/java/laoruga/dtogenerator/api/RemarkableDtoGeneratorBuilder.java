//package laoruga.dtogenerator.api;
//
//import laoruga.dtogenerator.api.markup.generators.IGenerator;
//import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
//import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
//import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
//import lombok.NonNull;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//public class RemarkableDtoGeneratorBuilder extends DtoGeneratorBuilder {
//
//    protected Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksForAllFields = new HashMap<>();
//    protected Map<String, List<CustomRuleRemarkWrapper>> fieldCustomRuleRemarkMap = new HashMap<>();
//
//    public RemarkableDtoGeneratorBuilder addRuleRemarkForField(@NonNull String filedName,
//                                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
//        this.fieldCustomRuleRemarkMap.putIfAbsent(filedName, new LinkedList<>());
//        for (CustomRuleRemarkWrapper remark : ruleRemark) {
//            this.fieldCustomRuleRemarkMap.get(filedName).add(remark);
//        }
//        return this;
//    }
//
//    public RemarkableDtoGeneratorBuilder addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
//        for (CustomRuleRemarkWrapper remark : ruleRemarks) {
//            this.customRuleRemarksForAllFields.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
//            this.customRuleRemarksForAllFields.get(remark.getGeneratorClass()).add(remark);
//        }
//        return this;
//    }
//
//    @Override
//    public RemarkableDtoGeneratorBuilder setRuleRemarkForAllFields(BasicRuleRemark basicRuleRemark) {
//        super.setRuleRemarkForAllFields(basicRuleRemark);
//        return this;
//    }
//
//    @Override
//    public RemarkableDtoGeneratorBuilder addRuleRemarkForField(String filedName, IRuleRemark ruleRemark) {
//        super.addRuleRemarkForField(filedName, ruleRemark);
//        return this;
//    }
//
//    public RemarkableDtoGenerator build() {
//        return new RemarkableDtoGenerator(getFieldSimpleRuleRemarkMap(), customRuleRemarksForAllFields, this);
//    }
//}
