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

#define DEBUG 1
/*
 * Notes when debug mode is on:
 * 	-no username checking
 * 	-username not added to users.txt
 * 	-no username file generation
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

char** getUsers();
bool checkUsername(char *user, char **users);
//int checkUsername(char *user);
void createUsername(char *user);
int showMenu();

void sendTweet(char *user, char **users);
Tweet parseTweet(char *tweetstr);
bool verifyTweet(Tweet tweet, char *user, char **users);
void publishTweet(Tweet tweet, char *user, char **users);


void viewNotifications(char *user);
void viewWall(char *user);

void printTweet(Tweet tweet);
void printPersonalTweet(PersonalTweet tweet);

//void addToArray(char *str, char **arr);
//void init_tweet(Tweet tweet);
Tweet init_tweet();
PersonalTweet init_personal_tweet();

void freeTweet(Tweet tweet);
void freePersonalTweet(PersonalTweet tweet);

char* getInput(char *input);
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
	if(!checkUsername(user, users)) {
		printf("User already exists! Terminating...\n");
		return 0;
	}

//	system("clear");
	createUsername(user);
	printf("Welcome, %s!\n", user);
//	while(1) {
//		system("clear");
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
	}
//	}

	free(user);
//	flush_stdin();
	return 0;
}

/**
 * Retrieves the list of usernames from <i>users.txt</i>
 * @return A 2d pointer containing all existing usernames
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
#if DEBUG == 0
	for(int i = 0; **(users + i) != '\0'; i++) {
//		printf("%s - %s\n", user, *(users + i));
		if(strcmp(user, *(users + i)) == 0) {
			return false;
		}
	}
#endif
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
#if DEBUG == 0
	char *filename = (char *) malloc((int)strlen(user) + 4);
	strcpy(filename, user);
	strcat(filename, ".txt");
	FILE *registry = fopen("users.txt", "a");
	FILE *user_f = fopen(filename, "w");
	fprintf(registry, "\n%s", user);
	fclose(registry);
	fclose(user_f);
#endif
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
	input = getInput(input);
	tweet = parseTweet(input);
	printTweet(tweet);
	if(verifyTweet(tweet, user, users)) {
		publishTweet(tweet, user, users);
	} else {
		printf("\nsending tweet failed\n");
	}
	freeTweet(tweet);
}

Tweet parseTweet(char *tweetstr) {
//	Tweet tweet;
	Tweet tweet = init_tweet();
//	init_tweet(tweet);
//	tweet.content = (char **)malloc(sizeof(char *));
//	tweet.tagged_people = (char **) malloc(sizeof(char *));
//	tweet.hashtags = (char **) malloc(sizeof(char*));
//	tweet.htag_i = 0;
//	tweet.tag_i = 0;
//	tweet.content_i = 0;

	printf("Input: %s\n", tweetstr); //TODO remove later
	for(int i = 0; *(tweetstr + i) != '\0'; i++) {
//		printf("i::%i = %c[%i]\n", i, *(tweetstr + i), *(tweetstr + i));
		if(*(tweetstr + i) == '#') {
			int chars = 0;
			tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
			*(tweet.hashtags + tweet.htag_i) = (char *)malloc(sizeof(char));
			char *word = *(tweet.hashtags + tweet.htag_i);
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
//			*(tweet.hashtags + tweet.htag_i) = word;
			tweet.htag_i++;
		}
		else if(*(tweetstr + i) == '@') {
			int chars = 0;
			tweet.tagged_people = (char **)realloc(tweet.tagged_people, sizeof(char *) * (tweet.tag_i + 1));
			*(tweet.tagged_people + tweet.tag_i) = (char *)malloc(sizeof(char));
			char *word = *(tweet.tagged_people + tweet.tag_i);
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
//			*(tweet.tagged_people + tweet.tag_i) = word;
			tweet.tag_i++;
		}
		else {
			int chars = 0;
			tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
			*(tweet.content + tweet.content_i) = (char *)malloc(sizeof(char));
//			printf("%i [%p]\n", tweet.content_i, *(tweet.content + tweet.content_i));
			char *word = *(tweet.content + tweet.content_i);
			for(; *(tweetstr + i) != ' ' && *(tweetstr + i) != '\0'; i++) {
//				printf("content::%i (%p - %p) = %c[%i]\n", i, tweet.content + tweet.content_i, word + chars, *(tweetstr + i), *(tweetstr + i));
				word = (char *)realloc(word, sizeof(char) * (chars + 1));
				*(word + chars) = *(tweetstr + i);
				chars++;
				if(*(tweetstr + i + 1) == '\0') {
					break;
				}
			}
			word = (char *)realloc(word, sizeof(char) * (chars + 1));
			*(word + chars) = '\0';
//			*(tweet.content + tweet.content_i) = word;
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
 * Displays the tweets put this user's file.
 *
 * @param user The current username
 */
void viewNotifications(char *user) {
	putc('\n', stdout);
//	Tweet *tweets = (Tweet *)malloc(sizeof(Tweet));
	char *filename = (char *)malloc(sizeof(char) * (int)strlen(user) + 4);
	FILE *file;
//	char **strs = (char **)malloc(sizeof(char *));
//	*strs = (char *)malloc(sizeof(char) * MAX_LEN);
//
	strcpy(filename, user);
	strcat(filename, ".txt");
	file = fopen(filename, "r");
	char str[MAX_LEN];
	for(int i = 0; fgets(str, MAX_LEN, file) != 0; i++) {
		PersonalTweet ptweet = init_personal_tweet();
		strtok(str, "\n");

		char *s = strtok (str,",");
		ptweet.sender = (char *)realloc(ptweet.sender, sizeof(char) * strlen(s));
		strcpy(ptweet.sender, s);
		s = strtok(NULL, ",");
		while (s != NULL) {
			if(s[0] == '#') {
				ptweet.hashtags = (char **)realloc(ptweet.hashtags, sizeof(char *) * (ptweet.htag_i + 1));
				*(ptweet.hashtags + ptweet.htag_i) = (char *)malloc(sizeof(char) * strlen(s));
//				*(ptweet.hashtags + ptweet.htag_i) = (char *)realloc(*ptweet.hashtags,
//						sizeof(char) * strlen(s) + 1);
				strcpy(*(ptweet.hashtags + ptweet.htag_i), s);
//				strcat(*(ptweet.hashtags + ptweet.htag_i), "\0");
//				printf("hashtag[%i]: %s - %i\n", ptweet.htag_i, *(ptweet.hashtags + ptweet.htag_i), (int)strlen(s));
				ptweet.htag_i++;
			} else if(s[0] == '@') {
				ptweet.tagged_people = (char **)realloc(ptweet.tagged_people,
						sizeof(char *) * (ptweet.tag_i + 1));
				*(ptweet.tagged_people + ptweet.tag_i) = (char *)malloc(sizeof(char) * strlen(s));
//				*(ptweet.tagged_people + ptweet.tag_i) = (char *)realloc(*ptweet.tagged_people,
//						sizeof(char) * strlen(s) + 1);
				strcpy(*(ptweet.tagged_people + ptweet.tag_i), s);
//				strcat(*(ptweet.tagged_people + ptweet.tag_i), "\0");
//				printf(" tagged[%i]: %s - %i\n", ptweet.tag_i, *(ptweet.tagged_people + ptweet.tag_i), (int)strlen(s));
				ptweet.tag_i++;
			} else {
				ptweet.content = (char **)realloc(ptweet.content,
						sizeof(char *) * (ptweet.content_i + 1));
				*(ptweet.content + ptweet.content_i) = (char *)malloc(sizeof(char) * strlen(s));
//				*(ptweet.content + ptweet.content_i) = (char *)realloc(*ptweet.content,
//						sizeof(char) * strlen(s) + 1);
				strcpy(*(ptweet.content + ptweet.content_i), s);
//				strcat(*(ptweet.content + ptweet.content_i), "\0");
//				printf("content[%i]: %s - %i\n", ptweet.content_i, *(ptweet.content + ptweet.content_i), (int)strlen(s));
				ptweet.content_i++;
			}
			s = strtok(NULL, ",");
		}

		printPersonalTweet(ptweet);
		putc('\n', stdout);

		free(s);
		freePersonalTweet(ptweet);
//		tweets = (Tweet *)realloc(tweets, sizeof(Tweet) * (i + 1));
//		strs = (char **)realloc(strs, sizeof(char *) * (i + 1));
//		*(strs + i + 1) = (char *)malloc(sizeof(char) * MAX_LEN);
	}
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
//	printf("TEST1: ");
//	for(int i = 0; i < 9; i++) {
//		printf("%s ", *(tweet.content + i));
//	}
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
	printf("\n");
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
 * Frees the memory used by the tweet.
 * @param tweet The tweet struct to be freed
 */
void freeTweet(Tweet tweet) {
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
 * Frees the memory used by the tweet.
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

char* getInput(char *input) {
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
 * Flushes all the contents of stdin.
 */
void flush_stdin() {
	int c = 0;
	while ((c = getchar()) != '\n' && c != EOF);
}
