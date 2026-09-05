package com.wms;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 架构守卫：用 ArchUnit 固化「模块化单体」的包边界（对应 CLAUDE.md 铁律）。
 * 只分析 main 源码，不纳入测试类。
 */
class ArchitectureTest {

    /** 业务域根包（common 为公共契约层，不参与「禁止跨域访问 service 实现」约束）。 */
    private static final String[] DOMAINS = {
            "com.wms.base", "com.wms.system", "com.wms.stock",
            "com.wms.inbound", "com.wms.outbound"
    };

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.wms");
    }

    @Test
    @DisplayName("铁律：业务域不得直接依赖其他域的 service 实现类（只走公共契约/事件）")
    void noCrossDomainServiceImplAccess() {
        for (String domain : DOMAINS) {
            for (String other : DOMAINS) {
                if (domain.equals(other)) {
                    continue;
                }
                noClasses()
                        .that().resideInAPackage(domain + "..")
                        .should().dependOnClassesThat().resideInAPackage(other + ".service.impl..")
                        .check(classes);
            }
        }
    }

    @Test
    @DisplayName("分层：Controller 不得直接访问 Mapper（必须经 Service）")
    void controllersDoNotAccessMappers() {
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..mapper..")
                .check(classes);
    }

    @Test
    @DisplayName("模块无环：业务域之间不得形成循环依赖")
    void modulesAreFreeOfCycles() {
        slices().matching("com.wms.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
