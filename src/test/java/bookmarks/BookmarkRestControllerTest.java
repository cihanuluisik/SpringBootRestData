package bookmarks;

import bookmarks.repository.Account;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.Bookmark;
import bookmarks.repository.BookmarkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
@AutoConfigureMockMvc
public class BookmarkRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String userName = "bdussault";

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Account account;

    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Autowired
    private BookmarkRepository bookmarkRepository;


    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void setup() throws Exception {
        assertNotNull("the JSON message converter must not be null", mappingJackson2HttpMessageConverter);

        this.bookmarkRepository.deleteAllInBatch();
        this.accountRepository.deleteAllInBatch();

        this.account = accountRepository.save(new Account(userName, "password"));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + userName, "A description")));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + userName, "A description")));
    }

    @Test
    public void userNotFoundGivenAbsentUserToGetAllBookmarks() throws Exception {
        mockMvc.perform(post("/george/bookmarks/").content(this.json(new Bookmark()))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userNotFoundGivenAbsentUserToGetSingleBookmark() throws Exception {
        mockMvc.perform(get("/george/bookmarks/1").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userNotFoundGivenAbsentUserToCreateBookmark() throws Exception {
        mockMvc.perform(post("/george/bookmarks")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void readSingleBookmark() throws Exception {
        Bookmark bookmark = this.bookmarkList.get(0);
        mockMvc.perform(get("/" + userName + "/bookmarks/"
                + bookmark.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(bookmark.getId().intValue())))
                .andExpect(jsonPath("$.uri", is(bookmark.getUri())))
                .andExpect(jsonPath("$.description", is(bookmark.getDescription())));
    }

    @Test
    public void readBookmarks() throws Exception {
        mockMvc.perform(get("/" + userName + "/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].uri", is("http://bookmark.com/1/" + userName)))
                .andExpect(jsonPath("$[0].description", is("A description")))
                .andExpect(jsonPath("$[1].id", is(this.bookmarkList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].uri", is("http://bookmark.com/2/" + userName)))
                .andExpect(jsonPath("$[1].description", is("A description")));
    }

    @Test
    public void createBookmark() throws Exception {
        String bookmarkJson = json(new Bookmark(
                this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information"));

        this.mockMvc.perform(post("/" + userName + "/bookmarks")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(bookmarkJson))
                .andExpect(status().isCreated());
    }


    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}