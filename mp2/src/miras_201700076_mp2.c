/*
 ============================================================================
 Name        : mp2.c
 Author      : Carlo P. Miras
 Version     :
 Copyright   : All copyrights belong to the author.
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_LEN 1024

#define DEBUG 1
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

typedef struct tweet_{
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
void publishTweet(Tweet tweet, char *user);

void viewNotifications(char *user);
void viewWall(char *user);
void logout(char *user, char **users);

//tweet output
void printTweet(Tweet tweet);
void printPersonalTweet(PersonalTweet tweet);
void printPublicTweet(PublicTweet tweet);

//memory management
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
	char *user = 0;
	char **users = 0;
	int opt = 0;

	if(argc < 2) {
		printf("No username entered! Terminating...\n");
		return 0;
	} else if(argc > 2) {
		printf("Invalid number of arguments! Terminating...\n");
		return 0;
	} else {
		user = (char *) malloc((char)(strlen(argv[1]) + 1));
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

	while(1) {
		printf("Welcome, %s!\n", user);
		opt = showMenu();
		bool out = false;
		users = getUsers();
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
#if DEBUG < 1
				logout(user, users);
#endif
				out = true;
				break;
		}
		if(out)break;
	}

	for(int i = 0; **users != '\0'; i++) {
		free(*(users + i));
	}
	free(users);
	free(user);
	return 0;
}

/**
 * Retrieves the list of usernames from <i>users.txt</i>
 * @return A pointer-to-pointer containing all existing usernames
 */
char** getUsers() {
	FILE *fusers = fopen("users.txt", "r");
	if(fusers == NULL) {
		fopen("users.txt", "a");
		fusers = fopen("users.txt", "r");
	}
	char u[1024];

	int n = 0;
	for(; fgets(u, 100, fusers) != 0; n++);
	rewind(fusers);

	char **users = (char **) malloc(sizeof(char *) * (n + 1));
	int i = 0;
	while(fscanf(fusers, "%s", u) != EOF) {
		*(users + i) = (char *)malloc(sizeof(char) * (int)strlen(u));
		strcpy(*(users + i), u);
		i++;
	}

	*(users + i) = (char *) malloc(sizeof(char));
	**(users + i) = '\0';
	fclose(fusers);
	return users;
}

/**
 * Checks if the username already exists in the user registry.
 * @param user The current username
 * @param users The list of all existing usernames
 * @return
 */
bool checkUsername(char *user, char **users) {
	for(int i = 0; **(users + i) != '\0'; i++) {
		if(strcmp(user, *(users + i)) == 0) {
			return false;
		}
	}
	return true;
}

/**
 * Creates a textfile for the current username.
 * @param user The current username
 */
void createUsername(char *user) {
	char *filename = (char *) malloc((int)strlen(user) + 4);
	strcpy(filename, user);
	strcat(filename, ".txt");
	FILE *registry = fopen("users.txt", "a");
	FILE *user_f = fopen(filename, "w");
	fprintf(registry, "%s\n", user);
	fclose(registry);
	fclose(user_f);
}

/**
 * Displays the menu and accepts the option selected by the user. Option is checked prior to return.
 * @return The option [int] selected by the user
 */
int showMenu() {
	int a;
	printf("(1) Send a Tweet\n");
	printf("(2) View Notifications\n");
	printf("(3) View Wall\n");
	printf("(4) Log Out\n");
	printf("Enter here: ");
	scanf("%i", &a);
	if(a < '1' || a > '4') {
		printf("Invalid option!\n");
		a = showMenu();
	}
	flush_stdin();
	return a;
}

/**
 * Handles the tweet-sending functionality of the program.
 * @param user The current username
 * @param users The list of all existing usernames
 */
void sendTweet(char *user, char **users) {
	char *input = (char *)malloc(1);
	Tweet tweet;

	printf("Enter tweet: ");
	input = get_input(input);
	tweet = parseTweet(input);
	printTweet(tweet);

#if DEBUG < 2
	if(verifyTweet(tweet, user, users)) {
		publishTweet(tweet, user);
	} else {
		printf("\nsending tweet failed\n");
	}
#endif

	freeTweet(tweet);
}

/**
 * Parses an input string into a Tweet struct.
 * @param tweetstr The input string to be parsed
 * @return The Tweet struct
 */
Tweet parseTweet(char *tweetstr) {
	Tweet tweet = init_tweet();

	printf("Input: %s\n", tweetstr);
	for(int i = 0; *(tweetstr + i) != '\0'; i++) {
		int chars = 0;
		char *word = (char *)malloc(sizeof(char) * MAX_LEN);
		for(; *(tweetstr + i) != ' ' && *(tweetstr + i) != '\0'; i++) {
			*(word + chars) = *(tweetstr + i);
			chars++;
		}
		*(word + chars) = '\0';
		word = (char *)realloc(word, sizeof(char) * strlen(word));

		if(*word == ' ') {
			continue;
		}
		if(*word == '#') {
			tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
			*(tweet.hashtags + tweet.htag_i) = word;
			tweet.htag_i++;
		}
		else if(*word == '@') {
			tweet.tagged_people = (char **)realloc(tweet.tagged_people, sizeof(char *) * (tweet.tag_i + 1));
			*(tweet.tagged_people + tweet.tag_i) = word;
			tweet.tag_i++;
		}
		else {
			tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
			*(tweet.content + tweet.content_i) = word;
			tweet.content_i++;
		}
	}

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
	for(int i = 0; i < tweet.tag_i; i++) {
		char *u = *(tweet.tagged_people + i);
		bool b = false;
		if(strcmp(u, user) == 0) {
			return false;
		}
		for(int j = 0; **(users + j) != '\0'; j++) {
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
 */
void publishTweet(Tweet tweet, char *user) {
	if(tweet.tag_i == 0) { //publishes to public_tweets.txt
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
		fclose(file);
	}
	for(int i = 0; i < tweet.tag_i; i++) { //publishes to specific user textfile/s
		char *tagged = *(tweet.tagged_people + i) + 1;
		char *filename = (char *)malloc(sizeof(char) * ((int)strlen(tagged) + 4));
		strcpy(filename, tagged);
		strcat(filename, ".txt");
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
	putchar('\n');
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
			int chars = 0;
			char *word = (char *)malloc(sizeof(char) * MAX_LEN);
			for(; *(str + i) != ',' && *(str + i) != ' ' && *(str + i) != '\0'; i++) {
				*(word + chars) = *(str + i);
				chars++;
			}
			*(word + chars) = '\0';
			word = (char *)realloc(word, sizeof(char) * (strlen(word) + 1));

			if(*word == ' ') {
				continue;
			}
			if(*word == '#') { // retrieves hashtags
				tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
				*(tweet.hashtags + tweet.htag_i) = word;
				tweet.htag_i++;
			}
			else if(*word == '@') { // retrieves tagged people
				tweet.tagged_people = (char **)realloc(tweet.tagged_people, sizeof(char *) *
						(tweet.tag_i + 1));
				*(tweet.tagged_people + tweet.tag_i) = word;
				tweet.tag_i++;
			}
			else { // retrieves content
				tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
				*(tweet.content + tweet.content_i) = word;
				tweet.content_i++;
			}
		}
		printPersonalTweet(tweet);
		printf("\n");
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
	putchar('\n');
	FILE *file = fopen("public_tweets.txt", "r");
	if(file == NULL) {
		file = fopen("public_tweets.txt", "a");
		file = fopen("public_tweets.txt", "r");
	}
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
			int chars = 0;
			char *word = (char *)malloc(sizeof(char) * MAX_LEN);
			for(; *(str + i) != ',' && *(str + i) != ' ' && *(str + i) != '\0'; i++) {
				*(word + chars) = *(str + i);
				chars++;
			}
			*(word + chars) = '\0';
			word = (char *)realloc(word, sizeof(char) * (strlen(word) + 1));\

			if(*word == ' ') {
				continue;
			}
			if(*word == '#') { // retrieves hashtags
				tweet.hashtags = (char **)realloc(tweet.hashtags, sizeof(char *) * (tweet.htag_i + 1));
				*(tweet.hashtags + tweet.htag_i) = word;
				tweet.htag_i++;
			}
			else { // retrieves content
				tweet.content = (char **)realloc(tweet.content, sizeof(char *) * (tweet.content_i + 1));
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

/**
 * Deletes the current username from users.txt and also its textfile.
 * @param user The current username
 * @param users The list of all existing usernames
 */
void logout(char *user, char **users) {
	users = getUsers();
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
}

/**
 * Prints a Tweet struct into STDOUT.
 * @param tweet The Tweet struct
 */
void printTweet(Tweet tweet) {
	if(tweet.content_i) {
		printf("Content: ");
		for(int i = 0; i < tweet.content_i; i++) {
			printf("%s ", *(tweet.content + i));
		}
		printf("\n");
	}
	if(tweet.htag_i) {
		printf("Hashtags: ");
		for(int i = 0; i < tweet.htag_i; i++) {
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

/**
 * Prints a PersonalTweet struct into STDOUT.
 * @param tweet The PersonalTweet struct
 */
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
		printf("\n");
	}
}

/**
 * Prints a PublicTweet struct into STDOUT.
 * @param tweet The PublicTweet struct
 */
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

/**
 * Retrieves a string of character input from STDIN.
 * @param input A char pointer that will contain the input
 * @return the variable 'input' containing the input
 */
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
 * @param str The char pointer to be chopped
 */
void chop_newline(char *str) {
	for(int j = 0; *(str + j) != '\0'; j++) {
		if(*(str + j) == '\n') {
			*(str + j) = '\0';
		}
	}
}

/**
 * Flushes all the contents of STDIN.
 */
void flush_stdin() {
	int c = 0;
	while ((c = getchar()) != '\n' && c != EOF);
}
