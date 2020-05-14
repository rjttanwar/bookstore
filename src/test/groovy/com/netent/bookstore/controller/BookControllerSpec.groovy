package com.netent.bookstore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.netent.bookstore.dao.BookRepository
import com.netent.bookstore.entity.Book
import com.netent.bookstore.model.BookDTO
import com.netent.bookstore.model.MediaPost
import com.netent.bookstore.service.BookService
import com.netent.bookstore.service.BookServiceImpl
import com.netent.bookstore.service.MediaService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.HttpServerErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.persistence.EntityManager

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerSpec extends Specification {

    @Shared
    ObjectMapper mapper = new ObjectMapper()

    BookRepository repo = Mock()
    EntityManager entityManager = Mock()
    MediaService mediaServiceMock = Mock()
    BookService bookService = new BookServiceImpl(entityManager, repo, mediaServiceMock)
    BookController bookController
    MockMvc mockMvc


    def setup() {
        bookController = new BookController(bookService)
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build()
    }

    def "Save a book"() {

        given:
        Map b = [
                isbn  : 134,
                title : "Pummy",
                author: "Pummy Dummy Author",
                price : 3.76
        ]
        Book book = new Book(
                "id": 1,
                "isbn": 134,
                "title": "Pummy",
                "author": "Pummy Dummy Author",
                "price": 3.76,
                "count": 1
        )
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Map> requestBody = new HttpEntity<>(b, headers)
        repo.findByIsbn(_ as Long) >> Optional.empty()
        repo.save(_ as Book) >> book

        when:
        MvcResult mvcResult = mockMvc.perform(post('/v1/book')
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(b)))
                .andExpect(status().isOk())
                .andReturn()

        then:
        noExceptionThrown()
    }

    def "Find a book"() {
        given:
        repo.findByIsbn(_ as Long) >> Optional.empty()
        expect:
        mockMvc.perform(get('/v1/book/986786'))
                .andExpect(status().is(404))
                .andReturn()
    }

    def "Search Media with book not found"() {
        given:
        repo.findByIsbn(_ as Long) >> Optional.empty()
        expect:
        mockMvc.perform(get('/v1/book/media/12345'))
                .andExpect(status().is(404))
                .andReturn()
    }

    def "Search Media with media api down"() {
        given:
        Book book = new Book(
                "id": 1,
                "isbn": 134,
                "title": "Pummy",
                "author": "Pummy Dummy Author",
                "price": 3.76,
                "count": 1
        )
        repo.findByIsbn(_ as Long) >> Optional.of(book)
        mediaServiceMock.searchMedia() >> { throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE) }
        when:
        mockMvc.perform(get('/v1/book/media/12345'))
                .andExpect(status().is(500))
                .andReturn()
        then:
        thrown(Exception.class)
    }

    def "Search Media with media api working"() {
        given:
        Book book = new Book(
                "id": 1,
                "isbn": 134,
                "title": "testString",
                "author": "Pummy Dummy Author",
                "price": 3.76,
                "count": 1
        )
        repo.findByIsbn(_ as Long) >> Optional.of(book)
        mediaServiceMock.searchMedia() >> getMediaPosts()
        when:
        MvcResult mvcResult = mockMvc.perform(get('/v1/book/media/12345'))
                .andExpect(status().isOk())
                .andReturn()
        then:
        3 == mapper.readValue(mvcResult.response.contentAsString, List.class).size()
    }

    def "Buy a book when not present"() {
        given:
        repo.findByIsbn(_ as Long) >> Optional.empty()
        expect:
        mockMvc.perform(get('/v1/book/buy/986786'))
                .andExpect(status().is(404))
                .andReturn()
    }

    @Unroll
    def "Buy a book "() {
        given:
        repo.findByIsbn(_ as Long) >> Optional.of(getBookWithCount(intialBookCount))
        when:
        mockMvc.perform(get('/v1/book/buy/986786'))
                .andExpect(status().isOk())
                .andReturn()
        then:
        noExceptionThrown()
        methodCallCount * repo.save(_)

        where:
        intialBookCount || methodCallCount
        2               || 1
        1               || 2
    }

    private static Book getBookWithCount(int count) {
        return new Book(
                "id": 1,
                "isbn": 134,
                "title": "testString",
                "author": "Pummy Dummy Author",
                "price": 3.76,
                "count": count
        )
    }

    private static List<MediaPost> getMediaPosts() {
        List<MediaPost> posts =
                [new MediaPost(
                        "userId": 2,
                        "id": 12,
                        "title": "in testString tempore odit est dolorem",
                        "body": "itaque id aut "
                ),
                 new MediaPost(
                         "userId": 2,
                         "id": 13,
                         "title": "saepe quo animi",
                         "body": "commodi quo doloremque\niste corrupti reiciendis voluptatem eius rerum\nsit cumque quod eligendi laborum minima\nperferendis recusandae assumenda consectetur porro architecto ipsum ipsam"
                 ),
                 new MediaPost(
                         "userId": 2,
                         "id": 14,
                         "title": "voluptatem eligendi optio",
                         "body": "testString dolorum perferendis illo voluptas\nnon doloremque neque facere\nad qui dolorum molestiae beatae\nsed aut voluptas totam sit illum"
                 ),
                 new MediaPost(
                         "userId": 2,
                         "id": 15,
                         "title": "eveniet quod temporibus",
                         "body": "reprehenderit quos placeat\nvelit minima officia dolores impedit repudiandae molestiae namtestString"
                 ),
                 new MediaPost(
                         "userId": 2,
                         "id": 16,
                         "title": "sint suscipit perspiciatis velit dolorum rerum ipsa laboriosam odio",
                         "body": "suscipit nam nisi quo aperiam aut\nasperiores eos fugit maiores voluptatibus quia\nvoluptatem quis ullam qui in alias quia est\nconsequatur magni mollitia accusamus ea nisi voluptate dicta"
                 )]
        return posts
    }
}
