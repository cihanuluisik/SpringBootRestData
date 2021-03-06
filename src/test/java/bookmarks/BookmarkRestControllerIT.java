package bookmarks;

import bookmarks.exception.ApiError;
import bookmarks.exception.UserNotFoundException;
import bookmarks.repository.Account;
import bookmarks.repository.AccountRepository;
import bookmarks.repository.Bookmark;
import bookmarks.repository.BookmarkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookmarkRestControllerIT {

    @LocalServerPort
    private int port;

    private String base;

    @Autowired
    TestRestTemplate testRestTemplate;

    @Before
    public void setUp() throws Exception {
        base = "http://localhost:" + port + "/";
        this.bookmarkRepository.deleteAllInBatch();
        this.accountRepository.deleteAllInBatch();
        this.account = accountRepository.save(new Account(userName, "password"));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + userName, "A description")));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + userName, "A description")));
    }

    private String userName = "bdussault";

    private Account account;

    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Autowired
    private BookmarkRepository bookmarkRepository;


    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void userNotFound() throws Exception {
        String notExistingUser = "userBBBCCCDDDDD";
        ResponseEntity<ApiError> result = testRestTemplate.getForEntity(base + "/" + notExistingUser + "/bookmarks/", ApiError.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody().getMessage()).startsWith(notExistingUser);

    }

    @Test
    public void givenSingleBookMarkGetShouldReturnIt() throws Exception {
        Bookmark bookmark = this.bookmarkList.get(0);

        String urlToCall = base + userName + "/bookmarks/" + bookmark.getId();
        ResponseEntity<Bookmark> result = testRestTemplate.getForEntity(urlToCall, Bookmark .class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(result.getBody()).isEqualToIgnoringGivenFields(bookmark, "account");
    }


     @Test
    public void givenTwoBookmarksGetAllShouldReturnAll() throws Exception {

        String urlToCall = base + userName + "/bookmarks";

        ResponseEntity<List<Bookmark>> result =
                 testRestTemplate.exchange(urlToCall, HttpMethod.GET, null, new ParameterizedTypeReference<List<Bookmark>>() {
                         });

        // or ResponseEntity<Bookmark[]> result2=    testRestTemplate.getForEntity(urlToCall, Bookmark[].class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);

        assertThat(result.getBody()).hasSize(bookmarkList.size());
        assertThat(result.getBody().get(0)).isEqualToIgnoringGivenFields(bookmarkList.get(0), "account");
        assertThat(result.getBody().get(1)).isEqualToIgnoringGivenFields(bookmarkList.get(1), "account");
    }


    @Test
    public void givenABookmarkPostedToCreateThenShouldCreate() throws Exception {

        Bookmark bookmark = new Bookmark(
                this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information");

        String urlToCall = base + userName + "/bookmarks";

        ResponseEntity<Bookmark> result = testRestTemplate.postForEntity(urlToCall, bookmark, Bookmark.class );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation().toString()).startsWith(urlToCall + "/");

    }

    @Test
    public void givenABookmarkPostedToCreateThenShouldCreateAndGetByLocationReturnedFromCreate() throws Exception {

        Bookmark bookmark = new Bookmark(
                this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information");

        String urlToCall = base + userName + "/bookmarks";

        ResponseEntity<Bookmark> saveResult = testRestTemplate.postForEntity(urlToCall, bookmark, Bookmark.class );

        assertThat(saveResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Bookmark> result = testRestTemplate.getForEntity(saveResult.getHeaders().getLocation(), Bookmark .class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(result.getBody()).isEqualToIgnoringGivenFields(bookmark, "account" ,"id");

    }


}