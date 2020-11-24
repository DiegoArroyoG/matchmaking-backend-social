package com.atomiclab.socialgamerbackend.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.atomiclab.socialgamerbackend.repository.FirebaseCrud;
import com.atomiclab.socialgamerbackend.service.FirebaseService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * Este servicio maneja la lectura y escritura en la base de datos Firestore.
 * @author Atomic Lab
 * @version 1.0
 */
@Service
public class FirebaseCrudImpl implements FirebaseCrud {
    @Autowired
    FirebaseService firebaseService;
    /**
     * Este m�todo recibe el identificador con que se quiere guardar el objeto, el nombre de la colecci�n en que se guardar� y el objeto que se guardar�
     * @param id identificador del objeto que se quiere guardar
     * @param collectionName Nombre de la coleccion
     * @param data Los datos que se van a guardar en la base
     * @return boolean que retorna si el metodo funciono o no
     */
    @Override
    public boolean save(String id, String collectionName, Object data) {
        CollectionReference userCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<WriteResult> writeInSnapshot = userCollection.document(id).create(data);
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n en que se guardar� y el objeto que se guardar�, el identificador se agrega de manera aleatoria.
     * @param collectionName Nombre de la coleccion donde se va a almacenar
     * @param data Los datos que se van a almacenar
     * @return boolean que retorna si el metodo funciono o no
     */
    @Override
    public boolean save(String collectionName, Object data) {
        CollectionReference userCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<WriteResult> writeInSnapshot = userCollection.document().create(data);
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Este m�todo recibe el identificador con que se quiere guard� el objeto, el nombre de la colecci�n en que se guard� y el objeto para actualizar.
     * @param id identificador del objeto
     * @param collectionName Nombre de la colecci�n donde se va a actualizar
     * @param data Los datos que se van a actualizar
     * @return boolean que retorna si el metodo funciono o no
     */
    @Override
    public boolean update(String id, String collectionName, Object data) {
        CollectionReference userCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<WriteResult> writeInSnapshot = userCollection.document(id).set(data);
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n y retorna la referencia que apunta a sus documentos, estas referencias son usadas para ejecutar queries
     * @param collectionName Nombre de la colleci�n
     * @return Referencia a la colecci�n
     */
    @Override
    public CollectionReference getCollection(String collectionName) {
        return firebaseService.getFirestore().collection(collectionName);
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n, el identificador de un documento de dicha colecci�n y el nombre de su sub-colecci�n, finalmente retorna la referencia que apunta a sus documentos, estas referencias son usadas para ejecutar queries
     * @param collectionName Nombre de la colecci�n que se quiere buscar
     * @param docId Identificador del documento a buscar
     * @param subcollectionName Nombre de la subcoleccion
     * @return Referencia a la subcolecci�n
     */
    @Override
    public CollectionReference getSubCollection(String collectionName, String docId, String subcollectionName) {
        return firebaseService.getFirestore().collection(collectionName).document(docId).collection(subcollectionName);
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n, el identificador de un documento de dicha colecci�n, el nombre de su sub-colecci�n y el objeto que se guardar� en esa sub-colecci�n.
     * @param collectionName Nombre de la colecci�n que se quiere buscar
     * @param docId Identificador del documento a buscar
     * @param subcollectionName Nombre de la subcoleccion
     * @param data Datos a guardar de la subcolecci�n
     * @return boolean que retorna si el metodo funciono o no
     */
    @Override
    public boolean saveSubCollection(String collectionName, String docId, String subcollectionName, Object data) {
        CollectionReference subCollection = firebaseService.getFirestore().collection(collectionName).document(docId)
                .collection(subcollectionName);
        ApiFuture<WriteResult> writeInSnapshot = subCollection.document().create(data);
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n y el identificador de un documento de dicha colecci�n, finalmente retorna el documento con ese Id
     * @param collectionName Nombre de la colecci�n que 
     * @param id identificador de un documento de dicha colecci�n
     * @return retorna el documento como tal
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    @Override
    public DocumentSnapshot getById(String collectionName, String id) throws InterruptedException, ExecutionException {
        CollectionReference dataCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<DocumentSnapshot> documentReference = dataCollection.document(id).get();
        return documentReference.get();
    }
    /**
     * Este m�todo recibe el nombre de la colecci�n y retorna toda la lista de documentos listos para convertir en objetos.
     * @param collectionName Nombre de la colecci�n que se quiere traer
     * @return Lista de los documentos de una colecci�n
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    @Override
    public List<DocumentSnapshot> get(String collectionName) throws InterruptedException, ExecutionException {
        List<DocumentSnapshot> data = new ArrayList<DocumentSnapshot>();
        CollectionReference dataCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<QuerySnapshot> querySnapshot = dataCollection.get();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            data.add(doc);
        }
        return data;
    }
    /**
     * Elimina un documento de una colecci�n
     * @param id Identificador de un documento de una colecci�n
     * @param collectionName Nombre de la colecci�n
     * @return boolean que retorna si el metodo funciono correctamente o no
     */
    @Override
    public boolean delete(String id, String collectionName) {
        CollectionReference userCollection = firebaseService.getFirestore().collection(collectionName);
        ApiFuture<WriteResult> writeInSnapshot = userCollection.document(id).delete();
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Elimina un documento de una subcolecci�n
     * @param id identificador del documento de una colecci�n
     * @param collectionName Nombre de la colecci�n a buscar
     * @param IdSubDocument Identificador del documento de la subcolecci�n
     * @param subCollectionName Nombre de la subcolecci�n
     * @return boolean que retorna si el metodo funciono correctamente o no
     */
    @Override
    public boolean deleteInSubCollection(String id, String collectionName, String IdSubDocument,
            String subCollectionName) {
        CollectionReference userCollection = firebaseService.getFirestore().collection(collectionName).document(id)
                .collection(subCollectionName);
        ApiFuture<WriteResult> writeInSnapshot = userCollection.document(IdSubDocument).delete();
        try {
            writeInSnapshot.get();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        } catch (ExecutionException e) {
            return false;
        }
        return true;
    }
    /**
     * Retorna el identificador de la colecci�n
     * @param collectionName Nombre de la colecci�n
     * @return identificador de la colecci�n
     */
    @Override
    public String createVoidAndGetId(String collectionName) {
        String id = firebaseService.getFirestore().collection(collectionName).document().getId();
        return id;
    }
    /**
     * Guarda un objeto dentro de una colecci�n sin tener el id previo
     * @param collectionName Nombre de la colecci�n
     * @param collectionData Objeto de la colecci�n
     * @param subCollectionName Nombre de la subcolecci�n
     * @param subCollectionData A�ade los datos de la subcolecci�n
     * @return boolean que retorna si el metodo funciono correctamente o no
     */
    @Override
    public boolean saveWithoutId(String collectionName, Object collectionData, String subCollectionName,
            Object subCollectionData) {
        String id = firebaseService.getFirestore().collection(collectionName).document().getId();
        boolean funciono = update(id, collectionName, collectionData);
        if (funciono) {
            CollectionReference ppp = firebaseService.getFirestore().collection(collectionName).document(id)
                    .collection(subCollectionName);
            ppp.add(subCollectionData);
        }
        return funciono;
    }
    /**
     * Metodo que retorna el id de un unico campo
     * @param collectionUniqueValue Valor unico dentro de la colecci�n
     * @param collectionName Nombre de la colecci�n
     * @return Identificador de la colecci�n que cumple
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    @Override
    public String getIdWithUniqueField(String collectionUniqueValue, String collectionName)
            throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> results = firebaseService.getFirestore().collection(collectionName)
                .whereEqualTo("nombre", collectionUniqueValue).get().get().getDocuments();
        String id = "";
        if (results.size() > 0) {
            id = results.get(0).getId();
        }
        return id;
    }
    /**
     * Palabra a buscar dentro de una colecci�n
     * @param subCollection Nombre de la subcolecci�n
     * @param attributeName Nombre del atributo donde se va a buscar
     * @param searchWord Palabra a buscar dentro de la subcolecci�n
     * @return 
     */
    @Override
    public Query collectionGroupSearch(String subCollection, String attributeName, String searchWord) {
        return firebaseService.getFirestore().collectionGroup(subCollection).whereEqualTo(attributeName, searchWord);
    }

}
