package study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void init() {
        queryFactory = new JPAQueryFactory(em);
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team2);
        Member member3 = new Member("member3", 30, team1);
        Member member4 = new Member("member4", 40, team2);
        Member member5 = new Member("null", 40, team2);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.flush();
        em.clear();
    }

    @Test
    public void startQueryDsl () {
        // given


        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertEquals("member1", findMember.getUsername());
    }

    @Test
    public void  searchTest() {
        // given
        Member member = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1").and(QMember.member.age
                        .eq(10))).fetchOne();

        assertEquals("member", member.getUsername());
    }

    @Test
    public void searchAndParamTest () {
        // given
        Member member = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"), (QMember.member.age.eq(10)))
                .fetchOne();

        assertEquals("member1", member.getUsername());
    }

    @Test
    public void resultFetch () {
        // given
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(QMember.member)
//                .fetchFirst();
//
//        long count = queryFactory
//                .selectFrom(member)
//                .fetchCount();

        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .fetchResults();


        long total = queryResults.getTotal();
        List<Member> contents = queryResults.getResults();

    }

    @Test
    public void sort () {
        // given
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

    }

    @Test
    public void  paging() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
    }

    @Test
    public void aggregation () {
        List<Tuple> fetch = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min(),
                        member.age.max()
                ).from(member)
                .fetch();

        Tuple tuple = fetch.get(0);
        System.out.println("tuple = " + tuple.get(member.count()));

    }
}
