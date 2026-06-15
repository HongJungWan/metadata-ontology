package com.hris.metadata.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.hris.metadata.shared.ddd.AggregateInternal;
import com.hris.metadata.shared.ddd.AggregateRoot;
import com.hris.metadata.shared.ddd.Subdomain;
import com.hris.metadata.shared.ddd.SubdomainType;
import com.hris.metadata.shared.ddd.ValueObject;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;

/**
 * cross-file 구조 규칙을 *정밀* 강제(훅의 휴리스틱과 달리 전체 클래스 그래프 분석).
 */
public final class DddRules {
    private DddRules() {}

    /** #3 도메인 순수성: 도메인 → application/infrastructure/presentation 의존 금지. */
    public static final ArchRule DOMAIN_PURITY = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..", "..infra..", "..adapter..", "..presentation..")
            .as("[DDD_DOMAIN_PURITY] 도메인은 바깥 레이어에 의존하지 않는다").allowEmptyShould(false);

    /** #4 DIP: 리포지토리 구현(*RepositoryImpl)은 infrastructure 에. */
    public static final ArchRule REPOSITORY_IMPL_IN_INFRA = classes().that()
            .haveSimpleNameEndingWith("RepositoryImpl")
            .should().resideInAnyPackage("..infrastructure..", "..infra..", "..adapter..")
            .as("[DDD_DIP] 리포지토리 구현체는 infrastructure 에 위치").allowEmptyShould(false);

    /** #9/#12 애그리거트 경계: @AggregateInternal 은 같은 패키지(애그리거트) 안에서만 접근. */
    public static final ArchRule AGGREGATE_ACCESS = classes().that()
            .areAnnotatedWith(AggregateInternal.class)
            .should(onlyBeAccessedWithinSameAggregate())
            .as("[DDD_AGGREGATE_ACCESS] 내부 엔티티는 애그리거트 루트를 통해서만 접근").allowEmptyShould(true);

    /** #13 ID 참조: 애그리거트 루트 필드가 다른 애그리거트 루트를 객체로 직접 참조 금지. */
    public static final ArchRule ID_REFERENCE_BETWEEN_AGGREGATES = fields().that()
            .areDeclaredInClassesThat().areAnnotatedWith(AggregateRoot.class)
            .should(notDirectlyReferenceAnotherAggregateRoot())
            .as("[DDD_ID_REFERENCE] 애그리거트 간 참조는 식별자(ID)로").allowEmptyShould(false);

    /** #16 VO 불변성: @ValueObject 의 필드는 final. */
    public static final ArchRule VALUE_OBJECT_IMMUTABLE = fields().that()
            .areDeclaredInClassesThat().areAnnotatedWith(ValueObject.class)
            .should().beFinal()
            .as("[DDD_VO_IMMUTABLE] 값 객체는 불변(final 필드)").allowEmptyShould(false);

    /** 필드 주입 금지(생성자 주입). */
    public static final ArchRule NO_FIELD_INJECTION = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .as("[DDD_NO_FIELD_INJECTION] 필드 주입 금지");

    /** 도메인 @Entity 는 @AggregateRoot 또는 @AggregateInternal 로 마킹되어야 한다. */
    public static final ArchRule DOMAIN_ENTITY_MARKED = classes().that()
            .resideInAPackage("..domain..")
            .and().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().beAnnotatedWith(AggregateRoot.class)
            .orShould().beAnnotatedWith(AggregateInternal.class)
            .as("[DDD_DOMAIN_ENTITY_MARKED] 도메인 @Entity 는 @AggregateRoot/@AggregateInternal 로 표시")
            .allowEmptyShould(false);

    /** @AggregateRoot 는 자기 타입을 반환하는 public static 팩토리 메서드를 가져야 한다. */
    public static final ArchRule AGGREGATE_ROOT_HAS_FACTORY = classes().that()
            .areAnnotatedWith(AggregateRoot.class)
            .should(haveAPublicStaticFactoryReturningSelf())
            .as("[DDD_AGGREGATE_ROOT_HAS_FACTORY] 애그리거트 루트는 정적 팩토리 메서드 보유")
            .allowEmptyShould(false);

    /**
     * 전략적 설계: CORE 서브도메인(@Subdomain(CORE))은 GENERIC 서브도메인(@Subdomain(GENERIC))에 의존하지 않는다.
     * (Term/Synonym 이 SqlPattern 에 의존 금지 — 핵심이 범용/대체가능 영역에 얽히는 것을 차단.)
     */
    public static final ArchRule CORE_NOT_DEPEND_ON_GENERIC = classes().that()
            .areAnnotatedWith(Subdomain.class).and(hasSubdomain(SubdomainType.CORE))
            .should(notDependOnGenericSubdomain())
            .as("[DDD_CORE_NOT_DEPEND_ON_GENERIC] CORE 서브도메인은 GENERIC 서브도메인에 의존하지 않는다")
            .allowEmptyShould(false);

    /**
     * 입력 모델 명명: application 레이어의 요청 입력은 *Command 로 통일한다(*Request 잔존 금지, DDD 2.3).
     */
    public static final ArchRule REQUEST_INPUT_IS_COMMAND = noClasses().that()
            .resideInAPackage("..application..")
            .should().haveSimpleNameEndingWith("Request")
            .as("[DDD_REQUEST_INPUT_IS_COMMAND] application 입력 모델은 *Command (잔존 *Request 금지)")
            .allowEmptyShould(false); // @RestController 실재 — vacuous 통과 금지

    /** 레이어 경계: application 은 infrastructure 에 의존하지 않는다(포트 사용 — DIP). 훅(휴리스틱)만 막던 경계를 CI 권위 게이트로도 강제한다. */
    public static final ArchRule APPLICATION_NOT_DEPEND_ON_INFRASTRUCTURE = noClasses().that()
            .resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..infra..")
            .as("[DDD_APP_NOT_DEPEND_ON_INFRA] application 은 infrastructure 에 의존하지 않는다(포트 사용)")
            .allowEmptyShould(false);

    /** 도메인 순수성(어노테이션): 도메인 클래스에 Spring 스테레오타입 금지(하네스 휴리스틱을 CI 로 승격). */
    public static final ArchRule DOMAIN_NO_SPRING_STEREOTYPES = classes().that().resideInAPackage("..domain..")
            .should(notBeAnnotatedWithAnyOf(
                    "org.springframework.stereotype.Service",
                    "org.springframework.stereotype.Component",
                    "org.springframework.stereotype.Repository",
                    "org.springframework.stereotype.Controller",
                    "org.springframework.web.bind.annotation.RestController",
                    "org.springframework.transaction.annotation.Transactional",
                    "org.springframework.beans.factory.annotation.Autowired"))
            .as("[DDD_DOMAIN_NO_SPRING] 도메인에 Spring 스테레오타입 금지").allowEmptyShould(false);

    /** 캡슐화: 도메인에 public setter 금지(@Data/@Setter 는 컴파일되면 setX 메서드로 관측됨). */
    public static final ArchRule DOMAIN_NO_SETTERS = classes().that().resideInAPackage("..domain..")
            .should(haveNoPublicSetter())
            .as("[DDD_DOMAIN_NO_SETTERS] 도메인에 public setter 금지").allowEmptyShould(false);

    /** 결정성: 도메인에서 시계/난수(.now()/UUID.randomUUID()/new Random()) 직접 호출 금지(주입받아라). */
    public static final ArchRule DOMAIN_NO_NONDETERMINISM = classes().that().resideInAPackage("..domain..")
            .should(notCallNonDeterministicApis())
            .as("[DDD_DOMAIN_DETERMINISM] 도메인에서 시계/난수 직접 호출 금지").allowEmptyShould(false);

    /** 도메인 서비스 무상태: @DomainService 의 인스턴스 필드는 final(주입 포트만). */
    public static final ArchRule DOMAIN_SERVICE_STATELESS = fields().that()
            .areDeclaredInClassesThat().areAnnotatedWith(com.hris.metadata.shared.ddd.DomainService.class)
            .and().doNotHaveModifier(JavaModifier.STATIC)
            .should().beFinal()
            .as("[DDD_DOMAIN_SERVICE_STATELESS] 도메인 서비스는 무상태(final 필드)").allowEmptyShould(true);

    /** VO 회귀 방지: 도메인 @Entity 의 (비static) String 필드 금지 → @Embedded 값 객체 사용(원시 집착 차단). */
    public static final ArchRule DOMAIN_ENTITY_NO_RAW_STRING = fields().that()
            .areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areDeclaredInClassesThat().areAnnotatedWith(jakarta.persistence.Entity.class)
            .and().doNotHaveModifier(JavaModifier.STATIC)
            .should().notHaveRawType(String.class)
            .as("[DDD_NO_PRIMITIVE_OBSESSION] 도메인 @Entity 의 String 필드 금지(값 객체로 포장)")
            .allowEmptyShould(true);

    private static ArchCondition<JavaClass> notBeAnnotatedWithAnyOf(String... annotationFqcns) {
        return new ArchCondition<>("not be annotated with Spring stereotypes") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                for (String fqcn : annotationFqcns) {
                    if (clazz.isAnnotatedWith(fqcn)) {
                        events.add(SimpleConditionEvent.violated(clazz,
                                clazz.getName() + " is annotated with forbidden " + fqcn));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveNoPublicSetter() {
        return new ArchCondition<>("have no public setter method") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                for (JavaMethod method : clazz.getMethods()) {
                    if (method.getModifiers().contains(JavaModifier.PUBLIC)
                            && method.getName().matches("set[A-Z].*")
                            && method.getRawParameterTypes().size() == 1) {
                        events.add(SimpleConditionEvent.violated(method,
                                method.getFullName() + " is a public setter (도메인 캡슐화 위반)"));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notCallNonDeterministicApis() {
        return new ArchCondition<>("not call now()/UUID.randomUUID()/new Random()") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                for (com.tngtech.archunit.core.domain.JavaCodeUnit unit : clazz.getCodeUnits()) {
                    unit.getMethodCallsFromSelf().forEach(call -> {
                        String target = call.getTarget().getFullName();
                        if (target.matches("java\\.time\\.[A-Za-z]+\\.now\\(\\)")
                                || target.equals("java.util.UUID.randomUUID()")) {
                            events.add(SimpleConditionEvent.violated(unit,
                                    unit.getFullName() + " calls non-deterministic " + target));
                        }
                    });
                    unit.getConstructorCallsFromSelf().forEach(call -> {
                        if (call.getTarget().getFullName().startsWith("java.util.Random.<init>")) {
                            events.add(SimpleConditionEvent.violated(unit,
                                    unit.getFullName() + " constructs java.util.Random"));
                        }
                    });
                }
            }
        };
    }

    private static com.tngtech.archunit.base.DescribedPredicate<JavaClass> hasSubdomain(SubdomainType type) {
        return new com.tngtech.archunit.base.DescribedPredicate<>("@Subdomain(" + type + ")") {
            @Override
            public boolean test(JavaClass clazz) {
                return clazz.tryGetAnnotationOfType(Subdomain.class)
                        .map(s -> s.value() == type).orElse(false);
            }
        };
    }

    private static boolean isGenericSubdomain(JavaClass clazz) {
        return clazz.tryGetAnnotationOfType(Subdomain.class)
                .map(s -> s.value() == SubdomainType.GENERIC).orElse(false);
    }

    private static ArchCondition<JavaClass> notDependOnGenericSubdomain() {
        return new ArchCondition<>("not depend on @Subdomain(GENERIC) classes") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                for (Dependency dep : clazz.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass().getBaseComponentType();
                    if (!target.equals(clazz) && isGenericSubdomain(target)) {
                        events.add(SimpleConditionEvent.violated(dep,
                                clazz.getSimpleName() + " (CORE) depends on GENERIC subdomain "
                                        + target.getSimpleName()));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> onlyBeAccessedWithinSameAggregate() {
        return new ArchCondition<>("only be accessed within the same aggregate (package)") {
            @Override
            public void check(JavaClass internal, ConditionEvents events) {
                for (Dependency dep : internal.getDirectDependenciesToSelf()) {
                    JavaClass origin = dep.getOriginClass().getBaseComponentType();
                    if (!origin.equals(internal) && !origin.getPackageName().equals(internal.getPackageName())) {
                        events.add(SimpleConditionEvent.violated(dep,
                                origin.getName() + " reaches into aggregate-internal " + internal.getSimpleName()
                                        + " from outside its aggregate"));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveAPublicStaticFactoryReturningSelf() {
        return new ArchCondition<>("have a public static factory method returning its own type") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean hasFactory = false;
                for (JavaMethod method : clazz.getMethods()) {
                    if (method.getModifiers().contains(JavaModifier.PUBLIC)
                            && method.getModifiers().contains(JavaModifier.STATIC)
                            && method.getRawReturnType().equals(clazz)) {
                        hasFactory = true;
                        break;
                    }
                }
                if (!hasFactory) {
                    events.add(SimpleConditionEvent.violated(clazz,
                            clazz.getName() + " has no public static factory method returning "
                                    + clazz.getSimpleName()));
                }
            }
        };
    }

    private static ArchCondition<JavaField> notDirectlyReferenceAnotherAggregateRoot() {
        return new ArchCondition<>("not directly reference another aggregate root") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                JavaClass type = field.getRawType();
                if (type.isAnnotatedWith(AggregateRoot.class) && !type.equals(field.getOwner())) {
                    events.add(SimpleConditionEvent.violated(field,
                            field.getFullName() + " directly references aggregate root "
                                    + type.getSimpleName() + " (use its ID instead)"));
                }
            }
        };
    }
}
