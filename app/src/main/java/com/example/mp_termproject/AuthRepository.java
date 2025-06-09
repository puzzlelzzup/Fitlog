package com.example.mp_termproject;

import android.util.Log;

import com.example.mp_termproject.model.Post;
import com.example.mp_termproject.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    // 리스너 인터페이스들
    public interface AuthListener { void onSuccess(); void onFailure(String message); }
    public interface UserSearchListener { void onSearchSuccess(List<User> users); void onSearchFailure(String message); }
    public interface FriendRequestListener { void onSuccess(); void onFailure(String message); }
    public interface FriendRequestInfoListener { void onListLoaded(List<FriendRequestInfo> requestInfoList); void onError(String message); }

    // 피드 기능에 필요한 리스너 인터페이스 추가
    public interface FeedPostsListener {
        void onPostsLoaded(List<Post> posts);
        void onError(String message);
    }

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void createUserAccount(String email, String password, String name, String imageUrl, AuthListener listener) {
        db.collection("users").whereEqualTo("name", name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    listener.onFailure("이미 사용중인 닉네임입니다.");
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                saveUserDetails(firebaseUser.getUid(), email, name, imageUrl, listener);
                            } else {
                                listener.onFailure("사용자 정보를 가져오지 못했습니다.");
                            }
                        } else {
                            listener.onFailure(authTask.getException().getMessage());
                        }
                    });
                }
            } else {
                listener.onFailure("닉네임 확인 중 오류가 발생했습니다.");
            }
        });
    }

    private void saveUserDetails(String userId, String email, String name, String imageUrl, AuthListener listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("email", email);
        user.put("name", name);
        user.put("imageUrl", imageUrl);
        user.put("bio", "");
        user.put("followerCount", 0L);
        user.put("followingCount", 0L);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("DB 저장 실패: " + e.getMessage()));
    }

    public void searchUsers(String searchText, UserSearchListener listener) {
        if (searchText == null || searchText.trim().isEmpty()) {
            listener.onSearchFailure("검색어가 비어있습니다.");
            return;
        }
        db.collection("users").orderBy("name").startAt(searchText).endAt(searchText + "\uf8ff").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSearchSuccess(task.getResult().toObjects(User.class));
                    } else {
                        listener.onSearchFailure(task.getException().getMessage());
                    }
                });
    }

    public void sendFriendRequest(String fromUid, String toUid, FriendRequestListener listener) {
        Map<String, Object> request = new HashMap<>();
        request.put("from", fromUid);
        request.put("to", toUid);
        request.put("status", "pending");
        request.put("createdAt", FieldValue.serverTimestamp());
        db.collection("friendRequests").add(request)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getFriendRequestsWithUserInfo(String currentUserUid, FriendRequestInfoListener listener) {
        db.collection("friendRequests").whereEqualTo("to", currentUserUid).whereEqualTo("status", "pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) { listener.onError(e.getMessage()); return; }
                    if (queryDocumentSnapshots == null) return;
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<DocumentSnapshot> requestDocs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot requestDoc : requestDocs) {
                        userTasks.add(db.collection("users").document(requestDoc.getString("from")).get());
                    }
                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        List<FriendRequestInfo> resultList = new ArrayList<>();
                        for (int i = 0; i < userSnapshots.size(); i++) {
                            User sender = ((DocumentSnapshot) userSnapshots.get(i)).toObject(User.class);
                            if (sender != null) {
                                resultList.add(new FriendRequestInfo(requestDocs.get(i).getId(), sender));
                            }
                        }
                        listener.onListLoaded(resultList);
                    }).addOnFailureListener(err -> listener.onError(err.getMessage()));
                });
    }

    public void acceptFriendRequest(String requestId, String fromUid, String toUid, FriendRequestListener listener) {
        WriteBatch batch = db.batch();
        DocumentReference followingRef = db.collection("users").document(fromUid).collection("following").document(toUid);
        batch.set(followingRef, new HashMap<>());
        DocumentReference followerRef = db.collection("users").document(toUid).collection("followers").document(fromUid);
        batch.set(followerRef, new HashMap<>());
        DocumentReference fromUserRef = db.collection("users").document(fromUid);
        batch.update(fromUserRef, "followingCount", FieldValue.increment(1));
        DocumentReference toUserRef = db.collection("users").document(toUid);
        batch.update(toUserRef, "followerCount", FieldValue.increment(1));
        DocumentReference requestRef = db.collection("friendRequests").document(requestId);
        batch.delete(requestRef);
        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void declineFriendRequest(String requestId, FriendRequestListener listener) {
        db.collection("friendRequests").document(requestId).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }


    // 새로운 피드 게시물 로딩
    public void getFeedPosts(String currentUserUid, FeedPostsListener listener) {
        Log.d("AuthRepo_Debug", "getFeedPosts 함수 호출됨. 사용자 UID: " + currentUserUid);

        // 피드에  팔로우한 유저 게시물 표시
        db.collection("users").document(currentUserUid).collection("following").get()
                .addOnSuccessListener(followingSnapshots -> {
                    Log.d("AuthRepo_Debug", "팔로잉 목록 가져오기 성공.");
                    List<String> followingUids = new ArrayList<>();
                    for (DocumentSnapshot doc : followingSnapshots) {
                        followingUids.add(doc.getId());
                    }

                    Log.d("AuthRepo_Debug", "팔로잉 수: " + followingUids.size());
                    if (!followingUids.isEmpty()) {
                        Log.d("AuthRepo_Debug", "팔로잉 UID 목록: " + followingUids.toString());
                    }

                    if (followingUids.isEmpty()) {
                        listener.onPostsLoaded(new ArrayList<>());
                        return;
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    Date sevenDaysAgo = calendar.getTime();

                    Log.d("AuthRepo_Debug", "이제 이 팔로잉 유저들의 게시물을 검색합니다...");

                    db.collection("posts")
                            .whereIn("userId", followingUids)
                            .whereGreaterThanOrEqualTo("timestamp", sevenDaysAgo)
                            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(postsSnapshot -> {
                                Log.d("AuthRepo_Debug", "게시물 검색 성공! 가져온 게시물 수: " + postsSnapshot.size());
                                List<Post> postList = postsSnapshot.toObjects(Post.class);
                                listener.onPostsLoaded(postList);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AuthRepo_Debug", "게시물 검색 실패!", e);
                                listener.onError("게시물을 불러오는 데 실패했습니다: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AuthRepo_Debug", "팔로잉 목록을 가져오는 데 실패!", e);
                    listener.onError("팔로잉 목록을 불러오는 데 실패했습니다: " + e.getMessage());
                });
    }
}