package lakshyaamez.tutorial.armeria.rest;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Default;
import com.linecorp.armeria.server.annotation.Delete;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import com.linecorp.armeria.server.annotation.RequestConverter;
import com.linecorp.armeria.server.annotation.RequestObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class BlogService {
  private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();

  /**
   * Creates a {@link BlogPost} from an {@link HttpRequest}. The {@link HttpRequest} is
   * converted into {@link BlogPost}.
   */
  @Post("/blogs")
  @RequestConverter(BlogPostRequestConverter.class)
  public HttpResponse createBlogPost(BlogPost blogPost) {
    blogPosts.put(blogPost.getId(), blogPost);

    return HttpResponse.ofJson(blogPost);
  }

  /**
   * Retrieves a {@link BlogPost} whose {@link BlogPost#getId()} is the {@code :id} in the path parameter.
   */
  @Get("/blogs/:id")
  public HttpResponse getBlogPost(@Param int id) {
    final BlogPost blogPost = blogPosts.get(id);
    return HttpResponse.ofJson(blogPost);
  }

  @Get("/blogs")
  @ProducesJson
  public Iterable<BlogPost> getBlogPosts(@Param @Default("true") boolean descending) {
    if (descending) {
      return blogPosts.entrySet().stream()
          .sorted(Comparator.comparingInt(Map.Entry::getKey))
          .map(Map.Entry::getValue)
          .collect(Collectors.toList());
    }
    return new ArrayList<>(blogPosts.values());
  }

  @Put("/blogs/:id")
  public HttpResponse updateBlogPost(@Param int id, @RequestObject BlogPost blogPost) {
    final BlogPost oldBlogPost = blogPosts.get(id);
    if (oldBlogPost == null) {
      return HttpResponse.of(HttpStatus.NOT_FOUND);
    }
    final BlogPost newBlogPost = new BlogPost(id, blogPost.getTitle(), blogPost.getContent(),
        oldBlogPost.getCreatedAt(), blogPost.getCreatedAt());

    blogPosts.put(id, newBlogPost);
    return HttpResponse.ofJson(newBlogPost);
  }

  @Blocking
  @Delete("/blogs/:id")
  @ExceptionHandler(BadRequestExceptionHandler.class)
  public HttpResponse deleteBlogPost(@Param int id) {
    final BlogPost removed = blogPosts.remove(id);
    if (removed == null) {
      throw new IllegalArgumentException("The blog post does not exist. ID: " + id);
    }
    return HttpResponse.of(HttpStatus.NO_CONTENT);
  }

}
