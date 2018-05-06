/*
 ============================================================================
 Name        : mp2.c
 Author      : Carlo P. Miras
 Version     :
 Copyright   : All copyrights belong to the author.
 Description : Hello World in C, Ansi-style
 ============================================================================
 */
//TODO check if all pointers are freed
//TODO ask if user textfile should not include own username in tagged people

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_LEN 1024

#define DEBUG 2
/*
 * Notes when debug mode is on level 1:
 * 	-no username checking
 * 	-username not added to users.txt
 * 	-no username file generation
 * 	-during logout, username not deleted from users.txt
 * 	-during logout, username file not deleted
 *
 * Notes when debug mode is on level 2:
 * 	-same as debug mode level 1
 * 	-entered tweet is not verified
 * 	-entered tweet is not written to respective files
 */

typedef struct tweet_{ //TODO if this is not allowable, reuse old solution [commented at the bottom of parseTweet()]
	char **content;
	char **tagged_people;
	char **hashtags;
	int content_i;
	int tag_i;
	int htag_i;
} Tweet;

typedef struct personal_tweet_{
	char *sender;
	char **content;
	char **tagged_people;
	char **hashtags;
	int content_i;
	int tag_i;
	int htag_i;
} PersonalTweet;

typedef struct public_tweet_{
	char *sender;
	char **content;
	char **hashtags;
	int content_i;
	int htag_i;
} PublicTweet;

char** getUsers();
bool checkUsername(char *user, char **users);
void createUsername(char *user);
int showMenu();

void sendTweet(char *user, char **users);
Tweet parseTweet(char *tweetstr);
bool verifyTweet(Tweet tweet, char *user, char **users);
void publishTweet(Tweet tweet, char *user, char **users);

void viewNotifications(char *user);
void viewWall(char *user);
void logout(char *user, char **users);

//tweet output
void printTweet(Tweet tweet);
void printPersonalTweet(PersonalTweet tweet);
void printPublicTweet(PublicTweet tweet);

//tweet memory management
Tweet init_tweet();
PersonalTweet init_personal_tweet();
PublicTweet init_public_tweet();
void freeTweet(Tweet tweet);
void freePersonalTweet(PersonalTweet tweet);
void freePublicTweet(PublicTweet tweet);

//tools
char* get_input(char *input);
void chop_newline(char *str);
void flush_stdin();


int main(int argc, char const *argv[])
{
	char *user = (char *) malloc(1024);
	char **users = 0;
	int opt = 0;

	if(argc < 2) {
		printf("No username entered! Terminating...\n");
		return 0;
	} else if(argc > 2) {
		printf("Invalid number of arguments! Terminating...\n");
		return 0;
	} else {
		user = (char *) realloc(user, (int)strlen(argv[1]));
		strcpy(user, argv[1]);
		printf("You entered: %s\n", user);
	}

	users = getUsers();

#if DEBUG < 1
	if(!checkUsername(user, users)) {
		printf("User already exists! Terminating...\n");
		return 0;
	}

	createUsername(user);
#endif

	printf("Welcome, %s!\n", user);
	opt = showMenu();
//	opt = 1;
	switch(opt) {
		case 1:
			sendTweet(user, users);
			break;
		case 2:
			viewNotifications(user);
			break;
		case 3:
			viewWall(user);
			break;
		case 4:
			logout(user, users);
			break;
	}

	free(user);
//	flush_stdin();
	return 0;
}

/**
 * Retrieves the list of usernames from <i>users.txt</i>
 * @return A pointer-to-pointer containing all existing usernames
 */
char** getUsers() {
	FILE *fusers = fopen("users.txt", "r");
	char u[1024];

	int n = 0;
	for(; fgets(u, 100, fusers) != 0; n++);
	rewind(fusers);

	char **users = (char **) malloc(sizeof(char *) * (n + 1));
	int i = 0;
	while(fscanf(fusers, "%s", u) != EOF) {
//		printf("%p\n", users);
		*(users + i) = (char *)malloc(sizeof(char) * (int)strlen(u));
		strcpy(*(users + i), u);
//		printf("from file: %s - %s[%p - %p]\n", u, *(users + i), users, u);
		i++;
	}
//	for(int j = 0; j < i; j++) {
//		printf("registry: %s[%p]\n", *(users + j), users);
//	}
	*(users + i) = (char *) malloc(sizeof(char));
	**(users + i) = '\0';
	fclose(fusers);
	return users;
}

/**
 * Checks if the username already exists in the user registry.
 * @param user The username
 * @param users The list of all existing usernames
 * @return
 */
bool checkUsername(char *user, char **users) {
	for(int i = 0; **(users + i) != '\0'; i++) {
//		printf("%s - %s\n", user, *(users + i));
		if(strcmp(user, *(users + i)) == 0) {
			return false;
		}
	}
	return true;
}

//int checkUsername(char *user) {
//	FILE *fusers = fopen("users.txt", "r");
//	char *u = (char *) malloc(1024);
//	while(fscanf(fusers, "%s", u) != EOF) {
//		if(strcmp(user, u) == 0) {
//			return 0;
//		}
//	}
//	free(u);
//	fclose(fusers);
//	return 1;
//}

void createUsername(char *user) {
	char *filename = (char *) malloc((int)strlen(user) + 4);
	strcpy(filename, user);
	strcat(filename, ".txt");
	FILE *registry = fopen("users.txt", "a");
	FILE *user_f = fopen(filename, "w");
	fprintf(registry, "\n%s", user);
	fclose(registry);
	fclose(user_f);
}

int showMenu() {
	int a;
	printf("(1) Send a Tweet\n");
	printf("(2) View Notifications\n");
	printf("(3) View Wall\n");
	printf("(4) Log Out\n");
	printf("Enter here: ");
	scanf("%i", &a);
	if(a < 1 || a > 4) {
		printf("Invalid option!\n");
		a = showMenu();
	}
	flush_stdin();
	return a;
}

void sendTweet(char *user, char **users) {
	char *input = (char *)malloc(1);
	Tweet tweet;

	printf("Enter tweet: ");
	input = get_input(input);
	tweet = parseTweet(input);
	printTweet(tweet);

#if DEBUG < 2
	if(verifyTweet(tweet, user, users)) {
		publishTweet(tweet, user, users);
	} else {
		printf("\nsending tweet failed\n");
	}
#endif

	freeTweet(tweet);
}

Tweet parseTweet(char *tweetstr) {
	Tweet tweet = init_tweet();

	printf("Input: %s\n", tweetstr); //TODO remove later
	for(int i = 0; *(tweetstr + i) != '\0'; i++) {
//		printf("i::%i = %c[%i]\n", i, *(tweetstr + i), *(tweetstr + i));
		if(*(tweetstr + i) == '#') {
			int chars = 0;
			tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
			char *word = (char *)malloc(sizeof(char));
			for(; *(tweetstr + i) != ' ' && *(tweetstr + i) != '\0'; i++) {
//				printf("hashtag::%i (%p - %p) = %c[%i]\n", i, tweet.hashtags + tweet.htag_i, word + chars, *(tweetstr + i), *(tweetstr + i));
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = *(tweetstr + i);
				chars++;
				if(*(tweetstr + i + 1) == '\0') {
					break;
				}
			}
			word = (char *)realloc(word, sizeof(char) * (chars + 1));
			*(word + chars) = '\0';
			*(tweet.hashtags + tweet.htag_i) = word;
			tweet.htag_i++;
		}
		else if(*(tweetstr + i) == '@') {
			int chars = 0;
			tweet.tagged_people = (char **)realloc(tweet.tagged_people, sizeof(char *) * (tweet.tag_i + 1));
			char *word = (char *)malloc(sizeof(char));
			for(; *(tweetstr + i) != ' ' && *(tweetstr + i) != '\0'; i++) {
//				printf("tagged::%i (%p - %p) = %c[%i]\n", i, tweet.tagged_people + tweet.tag_i, word + chars, *(tweetstr + i), *(tweetstr + i));
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = *(tweetstr + i);
				chars++;
				if(*(tweetstr + i + 1) == '\0') {
					break;
				}
			}
			word = (char *)realloc(word, sizeof(char) * (chars + 1));
			*(word + chars) = '\0';
			*(tweet.tagged_people + tweet.tag_i) = word;
			tweet.tag_i++;
		}
		else {
			int chars = 0;
			tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
//			printf("%i [%p]\n", tweet.content_i, *(tweet.content + tweet.content_i));
			char *word = (char *)malloc(sizeof(char));
			for(; *(tweetstr + i) != ' ' && *(tweetstr + i) != '\0'; i++) {
//				printf("content::%i (%p - %p) = %c[%i]\n", i, word + tweet.content_i, word + chars, *(tweetstr + i), *(tweetstr + i));
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = *(tweetstr + i);
				chars++;
				if(*(tweetstr + i + 1) == '\0') {
					break;
				}
			}
			word = (char *)realloc(word, sizeof(char) * (chars + 1));
			*(word + chars) = '\0';
			*(tweet.content + tweet.content_i) = word;
//			printf("%p -- %p\n", *(tweet.content + tweet.content_i), word);
			tweet.content_i++;
		}
	}

//	if(tweet.content_i > 0) {
//		tweet.content = (char **)realloc(tweet.content, tweet.content_i);
//		*(tweet.content + tweet.content_i) = (char *)malloc(1);
//		**(tweet.content + tweet.content_i) = 'x';
//	}
//	if(tweet.htag_i > 0) {
//		tweet.hashtags = (char **)realloc(tweet.hashtags, tweet.htag_i);
//		*(tweet.hashtags + tweet.htag_i) = (char *)malloc(1);
//		**(tweet.hashtags + tweet.htag_i) = 'x';
//	}
//	if(tweet.tag_i > 0) {
//		tweet.tagged_people = (char **)realloc(tweet.tagged_people, tweet.tag_i);
//		*(tweet.tagged_people + tweet.tag_i) = (char *)malloc(1);
//		**(tweet.tagged_people + tweet.tag_i) = 'x';
//	}

//	*(tweet.content + content_i) = (char *)malloc(1);
//	**(tweet.content + content_i) = 'x';
//	*(tweet.hashtags + htag_i) = (char *)malloc(1);
//	**(tweet.hashtags + htag_i) = 'x';
//	*(tweet.tagged_people + tag_i) = (char *)malloc(1);
//	**(tweet.tagged_people + tag_i) = 'x';

//	printf("%s, %s, %i\n", *(tweet.content), *(tweet.content + 1), contents);
//	**(tweet.tagged_people + tagschars) = '\0';
//	printf("%s[%i], %s[%i]\n", *(tweet.content + tweet.content_i), tweet.content_i, *(tweet.hashtags + tweet.htag_i), tweet.htag_i);

	return tweet;
}

/**
 * Checks if the tweet contains existing tagged people and if the user isn't included.
 * @param tweet The tweet to verify
 * @param user The current username
 * @param users The list of all usernames
 * @return
 */
bool verifyTweet(Tweet tweet, char *user, char **users) {
	for(int i = 0; i < tweet.tag_i; i++) { //TODO modify if current tweet struct is not allowable
		char *u = *(tweet.tagged_people + i);
		bool b = false;
		if(strcmp(u, user) == 0) {
			return false;
		}
		for(int j = 0; **(users + j) != '\0'; j++) {
//			printf("%s -- %s\n", u, *(users + j));
			if(strcmp(u + 1, *(users + j)) == 0) {
				b = true;
				break;
			}
		}
		if(b == false) {
			return false;
		}
	}

	return true;
}

/**
 * Publishes tweet to user textfiles.
 * @param tweet The tweet struct to be published
 * @param users The list of all usernames
 * @return
 */
void publishTweet(Tweet tweet, char *user, char **users) {//TODO simplify if possible
	if(tweet.tag_i == 0) {
		FILE *file = fopen("public_tweets.txt", "a");
		fprintf(file, "%s", user);
		if(tweet.content_i > 0) {
			fputc(',', file);
			for(int j = 0; j < tweet.content_i; j++) {
				char *content = *(tweet.content + j);
				if(j != tweet.content_i - 1) {
					fprintf(file, "%s ", content);
				}
				else {
					fprintf(file, "%s", content);
				}
			}
		}
		if(tweet.htag_i > 0) {
			for(int j = 0; j < tweet.htag_i; j++) {
				char *content = *(tweet.hashtags + j);
				fprintf(file, ",%s", content);
			}
		}
		fputc('\n', file);
	}
	for(int i = 0; i < tweet.tag_i; i++) {
		char *tagged = *(tweet.tagged_people + i) + 1;
		char *filename = (char *)malloc(sizeof(char) * ((int)strlen(tagged) + 4));
		strcpy(filename, tagged);
		strcat(filename, ".txt");
//		printf("%s\n", filename);
		FILE *file = fopen(filename, "a");
		fprintf(file, "%s", user);

		if(tweet.content_i > 0) {
			fputc(',', file);
			for(int j = 0; j < tweet.content_i; j++) {
				char *content = *(tweet.content + j);
				if(j != tweet.content_i - 1) {
					fprintf(file, "%s ", content);
				}
				else {
					fprintf(file, "%s", content);
				}
			}
		}
		if(tweet.tag_i > 0) {
			for(int j = 0; j < tweet.tag_i; j++) {
				char *content = *(tweet.tagged_people + j);
//				printf("%s -- %s\n", tagged - 1, content);
				if(strcmp(tagged - 1, content) == 0) {
					continue;
				} else {
					fprintf(file, ",%s", content);
				}
			}
		}
		if(tweet.htag_i > 0) {
			for(int j = 0; j < tweet.htag_i; j++) {
				char *content = *(tweet.hashtags + j);
				fprintf(file, ",%s", content);
			}
		}

		fputc('\n', file);
		free(filename);
		fclose(file);
	}
}

/**
 * Displays the tweets in this user's file.
 * @param user The current username
 */
void viewNotifications(char *user) {
	putc('\n', stdout);
	char *filename = (char *)malloc(sizeof(char) * (int)strlen(user) + 4);
	FILE *file;

	strcpy(filename, user);
	strcat(filename, ".txt");
	file = fopen(filename, "r");
	char *str = (char *)malloc(sizeof(char) * MAX_LEN);

	while(fgets(str, MAX_LEN, file) != 0) {
		PersonalTweet tweet = init_personal_tweet();
		int i = 0;
		chop_newline(str);
		for(; *(str + i) != ','; i++) { // retrieves sender first!
			tweet.sender = (char *)realloc(tweet.sender, sizeof(char) * (i + 2));
			*(tweet.sender + i) = *(str + i);
		}
		*(tweet.sender + i) = '\0';
		for(i++; *(str + i) != '\0'; i++) { // retrieves other data
			if(*(str + i) == '#') { // retrieves hashtags
				int chars = 0;
				tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
				char *word = (char *)malloc(sizeof(char));
				for(; *(str + i) != ',' && *(str + i) != '\0'; i++) {
					word = (char *)realloc(word, sizeof(char) * (chars + 1));
					*(word + chars) = *(str + i);
					chars++;
					if(*(str + i + 1) == '\0') {
						break;
					}
				}
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = '\0';
				*(tweet.hashtags + tweet.htag_i) = word;
				tweet.htag_i++;
			}
			else if(*(str + i) == '@') { // retrieves tagged people
				int chars = 0;
				tweet.tagged_people = (char **)realloc(tweet.tagged_people, sizeof(char *) * (tweet.tag_i + 1));
				char *word = (char *)malloc(sizeof(char));
				for(; *(str + i) != ',' && *(str + i) != '\0'; i++) {
					word = (char *)realloc(word, sizeof(char) * (chars + 1));
					*(word + chars) = *(str + i);
					chars++;
					if(*(str + i + 1) == '\0') {
						break;
					}
				}
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = '\0';
				*(tweet.tagged_people + tweet.tag_i) = word;
				tweet.tag_i++;
			}
			else { // retrieves content
				int chars = 0;
				tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
				char *word = (char *)malloc(sizeof(char));
				for(; *(str + i) != ',' && *(str + i) != ' ' && *(str + i) != '\0'; i++) {
					word = (char *)realloc(word, sizeof(char) * (chars + 1));
					*(word + chars) = *(str + i);
					chars++;
					if(*(str + i + 1) == '\0') {
						break;
					}
				}
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = '\0';
				*(tweet.content + tweet.content_i) = word;
				tweet.content_i++;
			}
		}
		printPersonalTweet(tweet);
		printf("\n\n");
		freePersonalTweet(tweet);
	}
	free(str);
	fclose(file);
}

/**
 * Displays all tweets from public_tweets.txt
 * @param user The current username
 */
void viewWall(char *user) {
	putc('\n', stdout);
	FILE *file = fopen("public_tweets.txt", "r");
	char *str = (char *)malloc(sizeof(char) * MAX_LEN);

	while(fgets(str, MAX_LEN, file) != 0) {
		PublicTweet tweet = init_public_tweet();
		int i = 0;
		chop_newline(str);
		for(; *(str + i) != ','; i++) { // retrieves sender first!
			tweet.sender = (char *)realloc(tweet.sender, sizeof(char) * (i + 2));
			*(tweet.sender + i) = *(str + i);
		}
		if(strcmp(tweet.sender, user) == 0) {
			continue;
		}
		*(tweet.sender + i) = '\0';
		for(i++; *(str + i) != '\0'; i++) { // retrieves other data
			if(*(str + i) == '#') { // retrieves hashtags
				int chars = 0;
				tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
				char *word = (char *)malloc(sizeof(char));
				for(; *(str + i) != ',' && *(str + i) != '\0'; i++) {
					word = (char *)realloc(word, sizeof(char) * (chars + 1));
					*(word + chars) = *(str + i);
					chars++;
					if(*(str + i + 1) == '\0') {
						break;
					}
				}
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = '\0';
				*(tweet.hashtags + tweet.htag_i) = word;
				tweet.htag_i++;
			}
			else { // retrieves content
				int chars = 0;
				tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
				char *word = (char *)malloc(sizeof(char));
				for(; *(str + i) != ',' && *(str + i) != ' ' && *(str + i) != '\0'; i++) {
					word = (char *)realloc(word, sizeof(char) * (chars + 1));
					*(word + chars) = *(str + i);
					chars++;
					if(*(str + i + 1) == '\0') {
						break;
					}
				}
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = '\0';
				*(tweet.content + tweet.content_i) = word;
				tweet.content_i++;
			}
		}
		printPublicTweet(tweet);
		printf("\n\n");
		freePublicTweet(tweet);
	}
	free(str);
	fclose(file);
}

void logout(char *user, char **users) {
#if DEBUG == 0
	FILE *registry = fopen("users.txt", "w");
	char *filename = (char *)malloc(sizeof(char) * strlen(user) + 4);
	strcpy(filename, user);
	strcat(filename, ".txt");
	for(int i = 0; **(users + i) != '\0'; i++) {
		if(strcmp(user, *(users + i)) != 0) {
			fprintf(registry, "%s\n", *(users + i));
		}
	}
	remove(filename);
	fclose(registry);
#endif
}

void printTweet(Tweet tweet) {
	if(tweet.content_i) {
		printf("Content: ");
		for(int i = 0; i < tweet.content_i; i++) {
//			printf("%c :: %s[%p] ", **(tweet.content + i), *(tweet.content + i), *(tweet.content + i));
//			printf("[%p]%s ",tweet.content + i, *(tweet.content + i));
			printf("%s ", *(tweet.content + i));
		}
		printf("\n");
	}
	if(tweet.htag_i) {
		printf("Hashtags: ");
		for(int i = 0; i < tweet.htag_i; i++) {
//			printf("%c :: %s[%p] ", **(tweet.content + i), *(tweet.hashtags + i), *(tweet.content + i));
//			printf("[%p]%s ", tweet.hashtags + i, *(tweet.hashtags + i));
			printf("%s ", *(tweet.hashtags + i));
		}
		printf("\n");
	}
	if(tweet.tag_i) {
		printf("Tagged people: ");
		for(int i = 0; i < tweet.tag_i; i++) {
			printf("%s ", *(tweet.tagged_people + i));
		}
		printf("\n");
	}
}

void printPersonalTweet(PersonalTweet tweet) {
	printf("@%s:\n", tweet.sender);
	if(tweet.content_i) {
		for(int i = 0; i < tweet.content_i; i++) {
			printf("%s ", *(tweet.content + i));
		}
	}
	if(tweet.htag_i) {
		for(int i = 0; i < tweet.htag_i; i++) {
			printf("%s ", *(tweet.hashtags + i));
		}
	}
	printf("\n");
	if(tweet.tag_i) {
		printf("People in this conversation: ");
		for(int i = 0; i < tweet.tag_i; i++) {
			printf("%s ", *(tweet.tagged_people + i));
		}
	}
}

void printPublicTweet(PublicTweet tweet) {
	printf("@%s:\n", tweet.sender);
	if(tweet.content_i) {
		for(int i = 0; i < tweet.content_i; i++) {
			printf("%s ", *(tweet.content + i));
		}
	}
	if(tweet.htag_i) {
		for(int i = 0; i < tweet.htag_i; i++) {
			printf("%s ", *(tweet.hashtags + i));
		}
	}
}

/**
 * Creates a new Tweet struct and initializes by allocating memory to the pointers.
 * @return The new Tweet struct
 */
Tweet init_tweet() {
	Tweet tweet;
	tweet.content = (char **)malloc(sizeof(char *));
	tweet.tagged_people = (char **) malloc(sizeof(char *));
	tweet.hashtags = (char **) malloc(sizeof(char*));
	tweet.htag_i = 0;
	tweet.tag_i = 0;
	tweet.content_i = 0;

	return tweet;
}

/**
 * Creates a new PersonalTweet struct  and initializes by allocating memory to the pointers
 * @return The new PersonalTweet struct
 */
PersonalTweet init_personal_tweet() {
	PersonalTweet ptweet;
	ptweet.sender = (char *)malloc(sizeof(char));
	ptweet.content = (char **)malloc(sizeof(char *));
	ptweet.tagged_people = (char **) malloc(sizeof(char *));
	ptweet.hashtags = (char **) malloc(sizeof(char*));
	ptweet.htag_i = 0;
	ptweet.tag_i = 0;
	ptweet.content_i = 0;

	return ptweet;
}

/**
 * Creates a new PublicTweet struct  and initializes by allocating memory to the pointers
 * @return The new PublicTweet struct
 */
PublicTweet init_public_tweet() {
	PublicTweet pubtweet;
	pubtweet.sender = (char *)malloc(sizeof(char));
	pubtweet.hashtags = (char **) malloc(sizeof(char*));
	pubtweet.htag_i = 0;

	return pubtweet;
}

/**
 * Frees the memory used by the Tweet struct.
 * @param tweet The tweet struct to be freed
 */
void freeTweet(Tweet tweet) {
	for(int i = 0; i < tweet.content_i; i++) {
//		printf("freeing %p\n", *(tweet.content + i));
		free(*(tweet.content + i));
	}
	for(int i = 0; i < tweet.htag_i; i++) {
		free(*(tweet.hashtags + i));
	}
	for(int i = 0; i < tweet.tag_i; i++) {
		free(*(tweet.tagged_people + i));
	}
	free(tweet.content);
	free(tweet.hashtags);
	free(tweet.tagged_people);
}

/**
 * Frees the memory used by the PersonalTweet struct.
 * @param tweet The tweet struct to be freed
 */
void freePersonalTweet(PersonalTweet tweet) {
	free(tweet.sender);
	for(int i = 0; i < tweet.content_i; i++) {
		free(*(tweet.content + i));
	}
	for(int i = 0; i < tweet.htag_i; i++) {
		free(*(tweet.hashtags + i));
	}
	for(int i = 0; i < tweet.tag_i; i++) {
		free(*(tweet.tagged_people + i));
	}
	free(tweet.content);
	free(tweet.hashtags);
	free(tweet.tagged_people);
}

/**
 * Frees the memory used by the PublicTweet struct.
 * @param tweet The tweet struct to be freed
 */
void freePublicTweet(PublicTweet tweet) {
	free(tweet.sender);
	for(int i = 0; i < tweet.content_i; i++) {
		free(*(tweet.content + i));
	}
	for(int i = 0; i < tweet.htag_i; i++) {
		free(*(tweet.hashtags + i));
	}
	free(tweet.content);
	free(tweet.hashtags);
}

char* get_input(char *input) {
	int i = 0;
	while(1) {
		input = (char *) realloc(input, i + 1);
		char c = getchar();
		if(c != '\n') {
			*(input + i) = c;
			i++;
		} else {
			*(input + i) = '\0';
			break;
		}
	}

	return input;
}

/**
 * Chops the supplied string to the first newline character, excluding the newline character.
 * @param str The string pointer
 */
void chop_newline(char *str) {
	for(int j = 0; *(str + j) != '\0'; j++) {
		if(*(str + j) == '\n') {
			*(str + j) = '\0';
		}
	}
}

/**
 * Flushes all the contents of stdin.
 */
void flush_stdin() {
	int c = 0;
	while ((c = getchar()) != '\n' && c != EOF);
}
