package com.atomiclab.socialgamerbackend.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.atomiclab.socialgamerbackend.domain.model.Comment;
import com.atomiclab.socialgamerbackend.domain.model.Person;
import com.atomiclab.socialgamerbackend.domain.model.Post;
import com.atomiclab.socialgamerbackend.domain.model.Reaction;
import com.atomiclab.socialgamerbackend.repository.FirebaseCrud;
import com.atomiclab.socialgamerbackend.repository.FirebaseSecAuth;
import com.atomiclab.socialgamerbackend.service.PostService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Query.Direction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    FirebaseCrud firebaseCrud;
    @Autowired
    FirebaseSecAuth firebaseSecAuth;

    private void setFeed(Post post, String token) throws InterruptedException, ExecutionException {
	String correo = firebaseSecAuth.getEmail(token);
        CollectionReference collection = firebaseCrud.getCollection("Amigos");
        Query requestsQuery1 = collection.whereEqualTo("persona1", correo).select("persona2");
        Query requestsQuery2 = collection.whereEqualTo("persona2", correo).select("persona1");
        for (DocumentSnapshot document : requestsQuery1.get().get().getDocuments()) {
            firebaseCrud.saveSubCollection("Persona", document.getData().get("persona2").toString(), "Feed", post);
        }
        for (DocumentSnapshot document : requestsQuery2.get().get().getDocuments()) {
            firebaseCrud.saveSubCollection("Persona", document.getData().get("persona1").toString(), "Feed", post);
        }
	firebaseCrud.saveSubCollection("Persona", correo, "Feed", post);
    }

    @Override
    public boolean uploadPost(Post post, String token) throws InterruptedException, ExecutionException {
        String correo = firebaseSecAuth.getEmail(token);
        Person person = firebaseCrud.getById("Persona", correo).toObject(Person.class);
        String id = correo.concat(LocalDateTime.now().toString());
        person.setPersona_id(correo);
        post.setPerson(person);
        post.setFecha(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        post.setReportado(false);
        post.setId(id);
        setFeed(post, token);
        return firebaseCrud.save(id, "Publicaciones", post);
    }

    @Override
    public List<Post> getFeed(String token) throws InterruptedException, ExecutionException {
        List<Post> feed = new ArrayList<Post>();
        CollectionReference feedRef = firebaseCrud.getSubCollection("Persona", firebaseSecAuth.getEmail(token), "Feed");
        Query feedQuery = feedRef.orderBy("fecha", Direction.DESCENDING).limit(20);
        ApiFuture<QuerySnapshot> querySnapshot = feedQuery.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            feed.add(document.toObject(Post.class));
        }
        return feed;
    }

    @Override
    public List<Comment> getComments(String idPublicacion, String token)
            throws InterruptedException, ExecutionException {
        List<Comment> comments = new ArrayList<Comment>();
        CollectionReference collection = firebaseCrud.getCollection("Comentarios");
        Query requestsQuery = collection.whereEqualTo("publicacion_id", idPublicacion);
        for(DocumentSnapshot document : requestsQuery.get().get().getDocuments()){
            comments.add(document.toObject(Comment.class));
        }
        return comments;
    }

    @Override
    public boolean makeComment(String token, Comment comment) throws InterruptedException, ExecutionException {
        String correo = firebaseSecAuth.getEmail(token);
        Person person = firebaseCrud.getById("Persona", correo).toObject(Person.class);
        person.setPersona_id(correo);
        comment.setPerson(person);
        comment.setFecha(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        return firebaseCrud.save("Comentarios", comment);
    }

    @Override
    public boolean like(String idPublicacion, String token) throws InterruptedException, ExecutionException {
        Reaction reaction = new Reaction(firebaseSecAuth.getEmail(token), idPublicacion);
        return firebaseCrud.save("Reacciones", reaction);
    }

    @Override
    public Integer getLikes(String idPublicacion) throws InterruptedException, ExecutionException {
        CollectionReference collection = firebaseCrud.getCollection("Reacciones");
        Query requestsQuery = collection.whereEqualTo("publicacion_id", idPublicacion);
        return requestsQuery.get().get().getDocuments().size();
    }

    @Override
    public List<Post> getPosts(String correo) throws InterruptedException, ExecutionException {
        List<Post> posts = new ArrayList<Post>();
        Post post;
        CollectionReference collection = firebaseCrud.getCollection("Publicaciones");
        Query requestsQuery = collection.whereEqualTo("person.persona_id", correo);
        for(DocumentSnapshot document : requestsQuery.get().get().getDocuments()){
            post = document.toObject(Post.class);
            post.setId(document.getId());
            posts.add(post);
        }
        return posts;
    }
}
